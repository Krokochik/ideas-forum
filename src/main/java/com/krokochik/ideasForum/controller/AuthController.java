package com.krokochik.ideasForum.controller;

import com.krokochik.ideasForum.model.Mail;
import com.krokochik.ideasForum.model.Role;
import com.krokochik.ideasForum.model.User;
import com.krokochik.ideasForum.repository.UserRepository;
import com.krokochik.ideasForum.service.mfa.MFAService;
import com.krokochik.ideasForum.service.MailService;
import com.krokochik.ideasForum.service.crypto.TokenService;
import com.krokochik.ideasForum.service.UserValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;

import static com.krokochik.ideasForum.Main.HOST;

@Controller
public class AuthController {

    @Autowired
    UserRepository userRepository;

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
    public String confirmMail(@RequestParam(name = "name") String name, @RequestParam(name = "token") String token, HttpServletRequest request, Model model) {
        if (userRepository.findByUsername(name) != null && userRepository.findByUsername(name).getMailConfirmationToken().equals(token)) {
            userRepository.setRoleById(Role.USER.name(), userRepository.findByUsername(name).getId());
            SecurityContextHolder.clearContext();
            model.addAttribute("name", name);
            model.addAttribute("pass", userRepository.findByUsername(name).getPassword());

            return "login";

        }
        return "redirect:/main";
    }

    @GetMapping("/mail-confirm")
    public String mailConfirmation(HttpServletResponse response) {
        SecurityContext context = getContext();
        if (isAuthenticated()) {
            Cookie cookie = new Cookie("avatar", null);
            cookie.setMaxAge(0);
            response.addCookie(cookie);
            return "redirect:/main";
        }
        if (hasRole(Role.ANONYM)) {
            if (!userRepository.findByUsername(context.getAuthentication().getName()).isConfirmMailSent()) {
                String userToken = new TokenService().generateToken();
                userRepository.setMailConfirmationTokenById(userToken, userRepository.findByUsername(context.getAuthentication().getName()).getId());
                Thread mailSending = new Thread(() -> {
                    Mail mail = new Mail();
                    mail.setReceiver(userRepository.findByUsername(context.getAuthentication().getName()).getEmail());
                    mail.setTheme("Email confirmation");
                    mail.setLink((host.contains("6606") ? "http://" : "https://") + host + "/confirm?name=" + context.getAuthentication().getName() + "&token=" + userToken);
                    mailService.sendActiveMail(mail, context.getAuthentication().getName());
                });
                mailSending.start();
                userRepository.setConfirmMailSentById(true, userRepository.findByUsername(getContext().getAuthentication().getName()).getId());
            }
            return "mail";
        }

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
                                @ModelAttribute(name = "pass") String password, @ModelAttribute(name = "passConf") String passwordConfirm,
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
                                @ModelAttribute(name = "nick") String name) {
        if (!name.isEmpty()) {
            if (userRepository.findByUsername(name) != null) {
                Thread mailSending = new Thread(() -> {
                    String passToken = new TokenService().generateToken();
                    Mail mail = new Mail();
                    mail.setTheme("Password abort");
                    mail.setReceiver(userRepository.findByUsername(name).getEmail());
                    mail.setLink((host.contains("6606") ? "http://" : "https://") + host + "/abortPass?name=" + name + "&token=" + passToken);
                    userRepository.setPasswordAbortTokenById(passToken, userRepository.findByUsername(name).getId());
                    mailService.sendEmail(mail, name, "abort.html");
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
    public String changeEmailGet(Model model,
                                 @RequestParam(name = "from", required = false) String from) {

        if (from != null)
            model.addAttribute("from", "/" + from);

        return "change-email";
    }

    @PostMapping("/change-email")
    public String changeEmail(Model model,
                              @ModelAttribute(name = "email") String email) {

        if (email.isEmpty() || !UserValidationService.validateEmail(email))
            return "change-email";

        userRepository.setEmailById(email, userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).getId());
        userRepository.setConfirmMailSentById(false, userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).getId());

        return "redirect:/mail-confirm";
    }

    @PostMapping("/sign-up")
    public String loginPage(Model model, @ModelAttribute(name = "username1") String name,
                            @ModelAttribute(name = "email1") String email,
                            @ModelAttribute(name = "password1") String pass) {

        User user = new User(name, email, pass);

        try {
            if (UserValidationService.validate(user)) {
                if (userRepository.findByUsername(user.getUsername()) == null) {
                    user.setRoles(Collections.singleton(Role.ANONYM));
                    userRepository.save(MFAService.writeSaltAndVerifier(user));
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


        model.addAttribute("name", name);
        model.addAttribute("pass", pass);

        return "login";
    }
}
