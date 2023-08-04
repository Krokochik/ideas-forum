package com.krokochik.ideasforum.controller.auth;

import com.krokochik.ideasforum.hcaptcha.HCaptchaClient;
import com.krokochik.ideasforum.hcaptcha.HCaptchaResponse;
import com.krokochik.ideasforum.model.db.User;
import com.krokochik.ideasforum.model.functional.Role;
import com.krokochik.ideasforum.repository.UserRepository;
import com.krokochik.ideasforum.service.UserValidator;
import com.krokochik.ideasforum.service.jdbc.UserService;
import com.krokochik.ideasforum.service.security.SecurityRoutineProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Base64;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Controller
public class AuthorizationController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;

    @Autowired
    SecurityRoutineProvider srp;

    record AuthData(String login, String email) {
    }

    @GetMapping("/proof-identity")
    public String proofIdentity() {
        return "password-enter";
    }

    @PostMapping("/proof-identity")
    public String proofIdentity(HttpSession session,
                                @RequestParam(name = "password", defaultValue = "") String password,
                                @RequestParam(name = "origin") String origin) {
        User user = userRepository.findByUsername(srp.getContext().getAuthentication().getName());
        boolean isIdConfirmed = password.equals(user.getPassword());
        if (!isIdConfirmed)
            return "redirect:/proof-identity?error=bad+password&origin=" + origin;
        else {
            session.setAttribute("isIdConfirmed", true);
            return "redirect:/" + origin;
        }
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("discord", false);
        model.addAttribute("github", false);
        return "login";
    }

    @GetMapping("/sign-up")
    public String signUp(Model model, HttpSession session,
                         @RequestParam(value = "oauth2", defaultValue = "", required = false) String oauth2Redirect) {
        // autofill data
        AuthData authData = (AuthData) session.getAttribute("authData");
        if (oauth2Redirect.equals("true") && authData == null) {
            if (session.getAttribute("oauth2Username") != null)
                if (session.getAttribute("oauth2Email") != null)
                    authData = new AuthData(session.getAttribute("oauth2Username").toString(),
                            session.getAttribute("oauth2Email").toString());
                else authData = new AuthData(session.getAttribute("oauth2Username").toString(), "");
        }
        model.addAttribute("authData", authData);
        session.setAttribute("authData", null);

        String oauth2Provider;
        if (session.getAttribute("oauth2Provider") != null)
            oauth2Provider = (String) session.getAttribute("oauth2Provider");
        else oauth2Provider = "Unknown";

        model.addAttribute("discord", oauth2Provider.equals("discord"));
        model.addAttribute("github", oauth2Provider.equals("github"));

        // mode ? sign up : login
        model.addAttribute("mode", true);

        return "login";
    }


    @Value("${hcaptcha.secret}")
    String secret;

    @Value("${hcaptcha.sitekey}")
    String sitekey;

    @PostMapping("/sign-up/{oauth2}")
    public String signUp(HttpSession session, HttpServletResponse httpResponse, HttpServletRequest httpRequest,
                         @RequestParam(name = "username") String name,
                         @RequestParam(name = "email") String email,
                         @RequestParam(name = "password") String pass,
                         @RequestParam(name = "h-captcha-response", required = false) String captchaToken,
                         @PathVariable(name = "oauth2", required = false) String oauth2) {
        User user = new User(name, email, pass);
        session.setAttribute("authData", new AuthData(name, email));
        try {
            HCaptchaResponse hcaptchaResponse = HCaptchaClient.verify(secret, captchaToken, sitekey);
            if (hcaptchaResponse == null || !hcaptchaResponse.isSuccess()) {
                httpResponse.setHeader("User Password", "wa'fuck, man?");
                return "redirect:/sign-up?areYouRobot";
            }
        } catch (IOException | InterruptedException exc) {
            log.error("An error occurred", exc);
            return "redirect:/sign-up?regErr";
        }

        Enum<? extends Enum<?>> validate = UserValidator.validate(user);
        if (validate.equals(UserValidator.Result.OK)) {
            if (userService.exists(user.getUsername())) {
                return "redirect:/sign-up?regErr&nameTakenErr";
            }
            Set<Role> userRoles = Collections.singleton(Role.ANONYM);
            if (oauth2 != null && !oauth2.isBlank() && session.getAttribute("oauth2Id") != null) {
                String oauth2Id = (String) session.getAttribute("oauth2Id");
                user.setOauth2Id(oauth2Id);
                if (session.getAttribute("oauth2Email") != null && session.getAttribute("oauth2Email").equals(email)) {
                    userRoles = Collections.singleton(Role.USER);
                }
                URL avatarUrl = (URL) session.getAttribute("oauth2AvatarUrl");
                if (avatarUrl != null) {
                    CompletableFuture.runAsync(() -> {
                        try {
                            BufferedImage image = ImageIO.read(avatarUrl);
                            ByteArrayOutputStream byteArrayOutStream = new ByteArrayOutputStream();
                            ImageIO.write(image, "png", byteArrayOutStream);
                            byte[] avatar = Base64.getEncoder().encode(byteArrayOutStream.toByteArray());
                            user.setAvatar(avatar);
                        } catch (IOException ioe) {
                            log.error("An error occurred during downloading avatar", ioe);
                        }
                    });
                }
            }
            user.setRoles(userRoles);
            boolean remember = session.getAttribute("oauth2Id") != null;

            userRepository.save(user);
            srp.authorizeUser(user, remember, srp.getContext(), httpRequest, httpResponse);

            session.removeAttribute("oauth2Id");
            session.removeAttribute("oauth2Username");
            session.removeAttribute("oauth2Email");
            session.removeAttribute("oauth2AvatarUrl");
            session.removeAttribute("authData");

            return "redirect:/email-validity-confirmation";
        } else if (validate.equals(UserValidator.Result.INVALID_USERNAME.SHORT)) {
            return "redirect:/sign-up?regErr&nameLenErr";
        } else if (validate.equals(UserValidator.Result.INVALID_USERNAME.BLANK)) {
            return "redirect:/sign-up?regErr&nameErr";
        } else if (validate.equals(UserValidator.Result.INVALID_PASSWORD)) {
            return "redirect:/sign-up?regErr&passInsecureErr";
        } else if (validate.equals(UserValidator.Result.INVALID_EMAIL)) {
            return "redirect:/sign-up?regErr&emailFormErr";
        }

        return "redirect:/login";
    }
}
