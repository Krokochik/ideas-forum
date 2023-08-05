package com.krokochik.ideasforum.controller.account;

import com.krokochik.ideasforum.model.db.User;
import com.krokochik.ideasforum.model.service.Mail;
import com.krokochik.ideasforum.service.MailService;
import com.krokochik.ideasforum.service.UserValidator;
import com.krokochik.ideasforum.service.crypto.TokenService;
import com.krokochik.ideasforum.service.jdbc.UserService;
import com.krokochik.ideasforum.service.security.SecurityRoutineProvider;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Controller
public class PasswordController {

    @Autowired
    UserService userService;

    @Autowired
    MailService mailService;

    @Autowired
    SecurityRoutineProvider srp;

    @GetMapping("/password-change-instructions")
    public String changePassword(Model model) {
        SecurityContext context = srp.getContext();
        CompletableFuture.runAsync(() -> {
            String passToken = new TokenService().generateToken();
            Mail mail = new Mail();
            mail.setTheme("Сброс пароля");
            mail.setReceiver(userService.findByUsernameOrUnknown(srp
                    .getContext().getAuthentication().getName()).getEmail());
            mail.setLink("https://ideas-forum.herokuapp.com/password-change?name="
                    + context.getAuthentication().getName()
                    + "&token=" + passToken);
            userService.setPasswordAbortTokenById(passToken, userService
                    .findByUsernameOrUnknown(context.getAuthentication().getName()).getId());
            try {
                mailService.sendEmail(mail, context.getAuthentication().getName(),
                        "", "password-change.html");
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }
            userService.setPasswordAbortSentById(true, userService
                    .findByUsernameOrUnknown(context.getAuthentication().getName()).getId());
        });

        model.addAttribute("from", "/settings");

        return "password-change-instructions";
    }

    @GetMapping("/password-change")
    public String changePassword(Model model,
                                 @RequestParam(name = "name") String name,
                                 @RequestParam(name = "token") String token) {
        Optional<User> user = userService.findByUsername(name);
        if (user.isPresent() && user.get().getPasswordAbortToken().equals(token)) {
            return "password-change";
        }
        return "redirect:/password-reset-request";
    }

    @PostMapping("/password-change")
    public String changePassword(Model model,
                                 @RequestParam(name = "pass") String password,
                                 @RequestParam(name = "passConf") String passwordConfirm,
                                 @RequestParam(name = "name") String name,
                                 @RequestParam(name = "token") String token) {
        Optional<User> user = userService.findByUsername(name);
        if (user.isPresent() && user.get().getPasswordAbortToken().equals(token)) {
            if (UserValidator.validatePassword(password)) {
                if (password.equals(passwordConfirm)) {
                    userService.setPasswordById(password, user.get().getId());
                    return "redirect:/login";
                }
                return "redirect:/password-change?name=" + name + "&token=" + token + "&error";
            }
            return "redirect:/password-change?name=" + name + "&token=" + token + "&passInsecureErr";
        }
        return "redirect:/password-reset-request";
    }

    @GetMapping("/password-reset-request")
    public String resetPassword(Model model) {
        return "password-reset-request";
    }

    @PostMapping("/password-reset-request")
    public String resetPassword(Model model,
                                @RequestParam(name = "nick") String name) {
        if (!name.isBlank()) {
            Optional<User> user = userService.findByUsername(name);
            if (user.isPresent()) {
                CompletableFuture.runAsync(() -> {
                    String token = new TokenService().generateToken();
                    Mail mail = new Mail();
                    mail.setTheme("Сброс пароля");
                    mail.setReceiver(user.get().getEmail());
                    mail.setLink("https://ideas-forum.herokuapp.com/password-change?name="
                            + name + "&token=" + token);
                    userService.setPasswordAbortTokenById(token, user.get().getId());
                    try {
                        mailService.sendEmail(mail, name, "", "password-change.html");
                    } catch (MessagingException e) {
                        throw new RuntimeException(e);
                    }
                    userService.setPasswordAbortSentById(true, user.get().getId());
                });
                return "redirect:/password-change-instructions";
            }
            return "redirect:/password-reset-request?notFoundErr";
        }
        return "redirect:/password-reset-request?nameErr";
    }
}
