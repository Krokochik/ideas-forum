package com.krokochik.ideasForum.controller;

import com.google.zxing.WriterException;
import com.krokochik.ideasForum.model.Mail;
import com.krokochik.ideasForum.model.Token;
import com.krokochik.ideasForum.model.User;
import com.krokochik.ideasForum.repository.UserRepository;
import com.krokochik.ideasForum.service.MailService;
import com.krokochik.ideasForum.service.crypto.TokenService;
import com.krokochik.ideasForum.service.mfa.MFAService;
import com.krokochik.ideasForum.service.mfa.QRCodeManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpSession;
import java.io.IOException;

import static com.krokochik.ideasForum.Main.HOST;

@Controller
public class SettingsController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    MailService mailService;

    @Autowired
    MFAService mfaService;

    @Autowired
    QRCodeManager qrCodeManager;

    String host = HOST;

    @GetMapping(value = "/mfa-qr", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<Resource> qrCode() {
        User user = userRepository.findByUsername(AuthController.getContext().getAuthentication().getName());
        Resource resource = new ByteArrayResource(user.getQrcode());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=qrcode.png")
                .body(resource);
    }

    @GetMapping("/settings")
    public String settingsPage(Model model, HttpSession session,
                               @CookieValue(name = "theme", required = false, defaultValue = "dark") String theme) {

        if (AuthController.isAuthenticated() ) {
            User user = userRepository.findByUsername(AuthController.getContext().getAuthentication().getName());
            if ((user.getQrcode() == null ||
                    !qrCodeManager.hasUserQrCode(user.getUsername()) ||
                    mfaService.getToken(user.getUsername()).isEmpty()) &&
                    !user.isMfaConnected())
            {
                try {
                    Token token = mfaService.addNewConnectionToken(user.getUsername());
                    qrCodeManager.addQrCode(token.toString(), user.getUsername());
                } catch (IOException | WriterException e) {
                    e.printStackTrace();
                }
            }
            model.addAttribute("token", mfaService.getToken(user.getUsername()).orElse(new Token()));
        }

        model.addAttribute("isIdConfirmed", session.getAttribute("isIdConfirmed") != null);
        session.removeAttribute("isIdConfirmed");
        model.addAttribute("theme", theme);
        return "settings";
    }

    @GetMapping("/password-change")
    public String changePassword(Model model) {
        SecurityContext context = AuthController.getContext();
        Thread mailSending = new Thread(() -> {
            String passToken = new TokenService().generateToken();
            Mail mail = new Mail();
            mail.setTheme("Сброс пароля");
            mail.setReceiver(userRepository.findByUsername(context.getAuthentication().getName()).getEmail());
            mail.setLink((host.contains("6606") ? "http://" : "https://") + host + "/abortPass?name=" + context.getAuthentication().getName() + "&token=" + passToken);
            userRepository.setPasswordAbortTokenById(passToken, userRepository.findByUsername(context.getAuthentication().getName()).getId());
            mailService.sendEmail(mail, context.getAuthentication().getName(), "", "abort.html");
            userRepository.setPasswordAbortSentById(true, userRepository.findByUsername(context.getAuthentication().getName()).getId());
        });
        mailSending.start();

        model.addAttribute("from", "/settings");

        return "abortPassNotify";
    }
}
