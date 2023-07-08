package com.krokochik.ideasForum.controller;

import com.krokochik.ideasForum.hcaptcha.HCaptchaClient;
import com.krokochik.ideasForum.hcaptcha.HCaptchaResponse;
import com.krokochik.ideasForum.model.Mail;
import com.krokochik.ideasForum.model.Role;
import com.krokochik.ideasForum.model.User;
import com.krokochik.ideasForum.repository.UserRepository;
import com.krokochik.ideasForum.service.MailService;
import com.krokochik.ideasForum.service.UserService;
import com.krokochik.ideasForum.service.UserValidationService;
import com.krokochik.ideasForum.service.crypto.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import static com.krokochik.ideasForum.Main.HOST;

@Controller
public class AuthController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;

    @Autowired
    MailService mailService;

    String host = HOST;

    public static boolean isAuthenticated() {
        for (GrantedAuthority authority : SecurityContextHolder.getContext().getAuthentication().getAuthorities()) {
            try {
                if (Role.valueOf(authority.getAuthority()) != Role.ANONYM)
                    return true;
            } catch (IllegalArgumentException exception) {
                return false;
            }
        }
        return false;
    }

    public static SecurityContext getContext() {
        return SecurityContextHolder.getContext();
    }

    public static boolean hasRole(Role role) {
        for (GrantedAuthority authority : SecurityContextHolder.getContext().getAuthentication().getAuthorities()) {
            try {
                if (Role.valueOf(authority.getAuthority()).equals(role))
                    return true;
            } catch (IllegalArgumentException exception) {
                return false;
            }
        }
        return false;
    }

    @GetMapping("/sign-up")
    public String loginPageGet(Model model) {
        model.addAttribute("mode", true);
        return "login";
    }

    @GetMapping("/confirm")
    public String confirmMail(@RequestParam(name = "name") String name,
                              @RequestParam(name = "token") String token,
                              @RequestParam(name = "newEmail", required = false) String newEmail,
                              HttpSession session, Model model) {
        User user = userRepository.findByUsername(name);
        if (user != null && user.getMailConfirmationToken().equals(token)) {
            if (newEmail != null && hasRole(Role.USER)) {
                userRepository.setEmailById(newEmail, user.getId());
                session.removeAttribute("newEmail");
                return "redirect:/settings";
            }

            userService.setRolesById(user.getId(), Collections.singleton(Role.USER));
            System.out.println(user.toString());
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                    new UserDetails() {
                        @Override
                        public Collection<? extends GrantedAuthority> getAuthorities() {
                            return Collections.singleton(new SimpleGrantedAuthority(Role.USER.name()));
                        }

                        @Override
                        public String getPassword() {
                            return user.getPassword();
                        }

                        @Override
                        public String getUsername() {
                            return user.getUsername();
                        }

                        @Override
                        public boolean isAccountNonExpired() {
                            return true;
                        }

                        @Override
                        public boolean isAccountNonLocked() {
                            return true;
                        }

                        @Override
                        public boolean isCredentialsNonExpired() {
                            return true;
                        }

                        @Override
                        public boolean isEnabled() {
                            return true;
                        }
                    }, user, Collections.singleton(new SimpleGrantedAuthority(Role.USER.name()))));

            return "redirect:/main";

        }
        return "redirect:/main";
    }

    @GetMapping("/mail-confirm")
    public String mailConfirmation(@RequestParam(name = "newEmail", required = false) String mode, HttpSession session, Model model) {
        Object temp = null;
        String newEmail = "";
        try {
            temp = session.getAttribute("newEmail");
        } catch (NullPointerException exception) {
            exception.printStackTrace();
        }
        if (hasRole(Role.USER) && (mode == null || temp == null))
            return "redirect:/main";

        if (temp != null)
            newEmail = temp.toString();
        SecurityContext context = getContext();

        User user = userRepository.findByUsername(context.getAuthentication().getName());
        if (hasRole(Role.ANONYM) || hasRole(Role.USER)) {
            if (!user.isConfirmMailSent()) {
                String userToken = new TokenService().generateToken();
                userRepository.setMailConfirmationTokenById(userToken, user.getId());
                boolean isAnonym = hasRole(Role.ANONYM);
                String finalNewEmail = newEmail;
                Thread mailSending = new Thread(() -> {
                    Mail mail = new Mail();
                    mail.setReceiver(isAnonym ? user.getEmail() : finalNewEmail);
                    mail.setTheme("Подтверждение почты");
                    mail.setLink((host.contains("6606") ? "http://" : "https://") + host + "/confirm?name=" + context.getAuthentication().getName() +
                            "&token=" + userToken + (!isAnonym ? "&newEmail=" + finalNewEmail : ""));
                    mailService.sendConfirmationMail(mail, user.getUsername(), isAnonym ? "На вашу почту был зарегестрирован новый аккаунт." : "К вашей почте был привязан аккаунт.");
                });
                mailSending.start();
                userRepository.setConfirmMailSentById(true, user.getId());
            }
            model.addAttribute("newEmail", newEmail);
            return "mail";
        }

        if (isAuthenticated())
            return "redirect:/main";
        return "redirect:/login";
    }

    @GetMapping("/abortPass")
    public String abortPass(Model model, @RequestParam(name = "name") String name, @RequestParam(name = "token") String token) {
        if (userRepository.findByUsername(name) != null && userRepository.findByUsername(name).getPasswordAbortToken().equals(token)) {
            return "abortPass";
        }
        return "redirect:/password-abort";
    }

    @PostMapping("/abortPass")
    public String abortPassPost(Model model,
                                @RequestParam(name = "pass") String password, @RequestParam(name = "passConf") String passwordConfirm,
                                @RequestParam(name = "name") String name, @RequestParam(name = "token") String token) {
        if (userRepository.findByUsername(name) != null && userRepository.findByUsername(name).getPasswordAbortToken().equals(token)) {
            if (UserValidationService.validatePassword(password)) {
                if (password.equals(passwordConfirm)) {
                    userRepository.setPasswordById(password, userRepository.findByUsername(name).getId());
                    return "redirect:/login";
                }
                return "redirect:/abortPass?name=" + name + "&token=" + token + "&error";
            }
            return "redirect:/abortPass?name=" + name + "&token=" + token + "&passInsecureErr";
        }
        return "redirect:/password-abort";
    }

    @GetMapping("/password-abort")
    public String abortPasswordGet(Model model) {
        return "pass";
    }

    @PostMapping("/password-abort")
    public String abortPassword(Model model,
                                @RequestParam(name = "nick") String name) {
        if (!name.isEmpty()) {
            if (userRepository.findByUsername(name) != null) {
                Thread mailSending = new Thread(() -> {
                    String passToken = new TokenService().generateToken();
                    Mail mail = new Mail();
                    mail.setTheme("Сброс пароля");
                    mail.setReceiver(userRepository.findByUsername(name).getEmail());
                    mail.setLink((host.contains("6606") ? "http://" : "https://") + host + "/abortPass?name=" + name + "&token=" + passToken);
                    userRepository.setPasswordAbortTokenById(passToken, userRepository.findByUsername(name).getId());
                    mailService.sendEmail(mail, name, "", "abort.html");
                    userRepository.setPasswordAbortSentById(true, userRepository.findByUsername(name).getId());
                });
                mailSending.start();
                return "redirect:/pass-abort-notify";
            }
            return "redirect:/password-abort?notFoundErr";
        }
        return "redirect:/password-abort?nameErr";
    }

    @GetMapping("pass-abort-notify")
    public String abortNotify() {
        return "abortPassNotify";
    }

    @GetMapping("/change-email")
    public String changeEmailGet(Model model, HttpSession session,
                                 @RequestParam(name = "from", required = false) String from) {

        if (session.getAttribute("newEmail") != null)
            return "redirect:/mail-confirm?newEmail";

        if (AuthController.hasRole(Role.ANONYM))
            model.addAttribute("isAnonym", true);
        else
            model.addAttribute("isAnonym", false);

        if (from != null)
            model.addAttribute("from", "/" + from);

        return "change-email";
    }

    @PostMapping("/change-email")
    public String changeEmail(Model model,
                              @RequestParam(name = "email") String email,
                              @RequestParam(name = "password", required = false) String password,
                              HttpSession session) {

        User user = userRepository.findByUsername(AuthController.getContext().getAuthentication().getName());

        if (hasRole(Role.ANONYM)) {
            userRepository.setEmailById(email, user.getId());
            userRepository.setConfirmMailSentById(false, user.getId());
            return "redirect:/mail-confirm";
        }

        if (email.isEmpty() || !UserValidationService.validateEmail(email) ||
                !password.equals(user.getPassword()))
            return "redirect:/change-email?error";

        userRepository.setConfirmMailSentById(false, user.getId());

        session.setAttribute("newEmail", email);
        return "redirect:/mail-confirm?newEmail";
    }

    @Value("${hcaptcha.secret}")
    String secret;

    @Value("${hcaptcha.sitekey}")
    String sitekey;

    @PostMapping("/sign-up")
    public String loginPage(Model model, HttpServletResponse httpResponse,
                            @RequestParam(name = "username") String name,
                            @RequestParam(name = "email") String email,
                            @RequestParam(name = "password") String pass,
                            @RequestParam(name = "h-captcha-response") String captchaToken) {

        User user = new User(name, email, pass);

        System.out.println(captchaToken);
        HCaptchaResponse response = null;
        try {
             response = HCaptchaClient.verify(secret, captchaToken, sitekey);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        if (response != null && response.isSuccess()) {
            try {
                if (UserValidationService.validate(user)) {
                    if (userRepository.findByUsername(user.getUsername()) == null) {
                        user.setRoles(Collections.singleton(Role.ANONYM));
                        userRepository.save(user);
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
            httpResponse.setStatus(403);
            return "redirect:/sign-up";
        }

        return "redirect:/login";
    }

    @GetMapping("/id-confirmation")
    public String idConfirmPage() {
        return "passPicker";
    }

    @PostMapping("/id-confirmation")
    public String idConfirm(HttpSession session, @RequestParam(name = "password", defaultValue = "") String password) {
        User user = userRepository.findByUsername(getContext().getAuthentication().getName());
        boolean isIdConfirmed = password.equals(user.getPassword());
        if (!isIdConfirmed)
            return "redirect:/id-confirmation?error";
        else {
            session.setAttribute("isIdConfirmed", true);
            return "redirect:/settings";
        }
    }
}
