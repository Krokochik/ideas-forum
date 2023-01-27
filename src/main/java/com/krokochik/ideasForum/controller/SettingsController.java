package com.krokochik.ideasForum.controller;

import com.krokochik.ideasForum.model.Mail;
import com.krokochik.ideasForum.repository.UserRepository;
import com.krokochik.ideasForum.service.TokenService;
import com.krokochik.ideasForum.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.krokochik.ideasForum.Main.HOST;

@Controller
public class SettingsController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    MailService mailService;

    String host = HOST;

    @GetMapping("/settings")
    public String settingsPage(Model model, HttpServletRequest request, HttpServletResponse response,
                               @RequestParam(name = "theme", defaultValue = "", required = false) String theme) {

        model.addAttribute("theme", theme.equals("light") ? "light" : "dark");
        return "settings";
    }
    @GetMapping("/password-change")
    public String changePassword(Model model) {
        SecurityContext context = AuthController.getContext();
        Thread mailSending = new Thread(() -> {
            String passToken = new TokenService().generateToken();
            Mail mail = new Mail();
            mail.setTheme("Password abort");
            mail.setReceiver(userRepository.findByUsername(context.getAuthentication().getName()).getEmail());
            mail.setLink((host.contains("6606") ? "http://" : "https://") + host + "/abortPass?name=" + context.getAuthentication().getName() + "&token=" + passToken);
            userRepository.setPasswordAbortTokenById(passToken, userRepository.findByUsername(context.getAuthentication().getName()).getId());
            mailService.sendEmail(mail, context.getAuthentication().getName(), "abort.html");
            userRepository.setPasswordAbortSentById(true, userRepository.findByUsername(context.getAuthentication().getName()).getId());
        });
        mailSending.start();

        model.addAttribute("from", "/settings");

        return "abortPassNotify";
    }
}
