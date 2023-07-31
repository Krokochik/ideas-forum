package com.krokochik.ideasForum.controller.account;

import com.krokochik.ideasForum.model.service.Mail;
import com.krokochik.ideasForum.repository.UserRepository;
import com.krokochik.ideasForum.service.UserValidationService;
import com.krokochik.ideasForum.service.crypto.TokenService;
import com.krokochik.ideasForum.service.MailService;
import com.krokochik.ideasForum.service.security.SecurityRoutineProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PasswordController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    MailService mailService;

    @Autowired
    SecurityRoutineProvider srp;

    @GetMapping("/password-change-instructions")
    public String changePassword(Model model) {
        SecurityContext context = srp.getContext();
        Thread mailSending = new Thread(() -> {
            String passToken = new TokenService().generateToken();
            Mail mail = new Mail();
            mail.setTheme("Сброс пароля");
            mail.setReceiver(userRepository.findByUsername(context.getAuthentication().getName()).getEmail());
            mail.setLink("https://ideas-forum.herokuapp.com/password-change?name=" + context.getAuthentication().getName() + "&token=" + passToken);
            userRepository.setPasswordAbortTokenById(passToken, userRepository.findByUsername(context.getAuthentication().getName()).getId());
            mailService.sendEmail(mail, context.getAuthentication().getName(), "", "password-change.html");
            userRepository.setPasswordAbortSentById(true, userRepository.findByUsername(context.getAuthentication().getName()).getId());
        });
        mailSending.start();

        model.addAttribute("from", "/settings");

        return "password-change-instructions";
    }

    @GetMapping("/password-change")
    public String changePassword(Model model, @RequestParam(name = "name") String name, @RequestParam(name = "token") String token) {
        if (userRepository.findByUsername(name) != null && userRepository.findByUsername(name).getPasswordAbortToken().equals(token)) {
            return "password-change";
        }
        return "redirect:/password-reset-request";
    }

    @PostMapping("/password-change")
    public String changePassword(Model model,
                                 @RequestParam(name = "pass") String password, @RequestParam(name = "passConf") String passwordConfirm,
                                 @RequestParam(name = "name") String name, @RequestParam(name = "token") String token) {
        if (userRepository.findByUsername(name) != null && userRepository.findByUsername(name).getPasswordAbortToken().equals(token)) {
            if (UserValidationService.validatePassword(password)) {
                if (password.equals(passwordConfirm)) {
                    userRepository.setPasswordById(password, userRepository.findByUsername(name).getId());
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
        if (!name.isEmpty()) {
            if (userRepository.findByUsername(name) != null) {
                Thread mailSending = new Thread(() -> {
                    String passToken = new TokenService().generateToken();
                    Mail mail = new Mail();
                    mail.setTheme("Сброс пароля");
                    mail.setReceiver(userRepository.findByUsername(name).getEmail());
                    mail.setLink("https://ideas-forum.herokuapp.com/password-change?name=" + name + "&token=" + passToken);
                    userRepository.setPasswordAbortTokenById(passToken, userRepository.findByUsername(name).getId());
                    mailService.sendEmail(mail, name, "", "password-change.html");
                    userRepository.setPasswordAbortSentById(true, userRepository.findByUsername(name).getId());
                });
                mailSending.start();
                return "redirect:/password-change-instructions";
            }
            return "redirect:/password-reset-request?notFoundErr";
        }
        return "redirect:/password-reset-request?nameErr";
    }
}