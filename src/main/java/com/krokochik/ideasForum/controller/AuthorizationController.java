package com.krokochik.ideasForum.controller;

import com.krokochik.ideasForum.hcaptcha.HCaptchaClient;
import com.krokochik.ideasForum.hcaptcha.HCaptchaResponse;
import com.krokochik.ideasForum.model.Role;
import com.krokochik.ideasForum.model.User;
import com.krokochik.ideasForum.repository.UserRepository;
import com.krokochik.ideasForum.service.UserValidationService;
import com.krokochik.ideasForum.service.MailService;
import com.krokochik.ideasForum.service.jdbc.UserService;
import com.krokochik.ideasForum.service.security.SecurityRoutineProvider;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Set;

@Controller
public class AuthorizationController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;

    @Autowired
    MailService mailService;

    @Autowired
    SecurityRoutineProvider srp;

    record AuthData(String login, String email) {}
    
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
    public String signUp(HttpSession session, HttpServletResponse httpResponse,
                            @RequestParam(name = "username") String name,
                            @RequestParam(name = "email") String email,
                            @RequestParam(name = "password") String pass,
                            @RequestParam(name = "h-captcha-response", required = false) String captchaToken,
                            @PathVariable(name = "oauth2", required = false) String oauth2) {

        User user = new User(name, email, pass);

        HCaptchaResponse response = null;
        try {
            response = HCaptchaClient.verify(secret, captchaToken, sitekey);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        session.setAttribute("authData", new AuthData(name, email));

        if ((response != null && response.isSuccess()) ||
                ((session.getAttribute("oauth2Username") != null) &&
                        (session.getAttribute("oauth2Id") != null) &&
                        oauth2.equals("oauth2"))) {
            try {
                if (UserValidationService.validate(user)) {
                    if (userRepository.findByUsername(user.getUsername()) == null) {
                        Set<Role> userRoles = Collections.singleton(Role.ANONYM);
                        if (oauth2.equals("oauth2") && (session.getAttribute("oauth2Id") != null)) {
                            String oauth2Id = (String) session.getAttribute("oauth2Id");
                            user.setOauth2Id(oauth2Id);

                            if (session.getAttribute("oauth2Email") != null)
                                if (session.getAttribute("oauth2Email").equals(email))
                                    userRoles = Collections.singleton(Role.USER);

                            URL avatarUrl;
                            if ((avatarUrl = (URL) session.getAttribute("oauth2AvatarUrl")) != null) {
                                byte[] avatar = new User().getAvatar();
                                try {
                                    BufferedImage image = ImageIO.read(avatarUrl);
                                    ByteArrayOutputStream byteArrayOutStream = new ByteArrayOutputStream();
                                    ImageIO.write(image, "png", byteArrayOutStream);
                                    avatar = Base64.encodeBase64(byteArrayOutStream.toByteArray());
                                } catch (IOException ignored) { }
                                user.setAvatar(avatar);
                            }
                        }

                        user.setRoles(userRoles);
                        userRepository.save(user);
                        srp.authorizeUser(SecurityContextHolder.getContext(), user);

                        session.setAttribute("oauth2Id", null);
                        session.setAttribute("oauth2Username", null);
                        session.setAttribute("oauth2Email", null);
                        session.setAttribute("oauth2AvatarUrl", null);
                        session.setAttribute("authData", null);
                    } else return "redirect:/sign-up?regErr&nameTakenErr";
                }
            } catch (UserValidationService.UsernameLengthException exception) {
                return "redirect:/sign-up?regErr&nameLenErr";
            } catch (NullPointerException exception) {
                return "redirect:/sign-up?regErr&nameErr";
            } catch (UserValidationService.PasswordInsecureException exception) {
                return "redirect:/sign-up?regErr&passInsecureErr";
            } catch (UserValidationService.EmailFormatException exception) {
                return "redirect:/sign-up?regErr&emailFormErr";
            }
        } else {
            httpResponse.setHeader("User Password", "wa'fuck, man?");
            return "redirect:/sign-up?areYouRobot";
        }

        return "redirect:/login";
    }
}
