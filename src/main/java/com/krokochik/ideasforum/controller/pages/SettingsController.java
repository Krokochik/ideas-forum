package com.krokochik.ideasforum.controller.pages;

import com.google.zxing.WriterException;
import com.krokochik.ideasforum.model.db.User;
import com.krokochik.ideasforum.model.service.Token;
import com.krokochik.ideasforum.service.MailService;
import com.krokochik.ideasforum.service.jdbc.UserService;
import com.krokochik.ideasforum.service.mfa.MFAService;
import com.krokochik.ideasforum.service.mfa.QRCodeManager;
import com.krokochik.ideasforum.service.security.SecurityRoutineProvider;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@Controller
public class SettingsController {

    @Autowired
    UserService userService;

    @Autowired
    MailService mailService;

    @Autowired
    MFAService mfaService;

    @Autowired
    QRCodeManager qrCodeManager;

    @Autowired
    SecurityRoutineProvider srp;

    @GetMapping("/settings")
    public String settings(Model model, HttpSession session,
                               @CookieValue(name = "theme", required = false, defaultValue = "dark") String theme) {
        Optional<User> userOptional = userService
                .findByUsername(srp.getContext().getAuthentication().getName());
        User user = userOptional.orElse(null);

        if (userOptional.isPresent() && srp.isAuthenticated()) {
            if ((user.getQrcode() == null ||
                    !qrCodeManager.hasUserQrCode(user.getUsername()) ||
                    mfaService.getToken(user.getUsername()).isEmpty()) &&
                    !user.isMfaActivated())
            {
                try {
                    Token token = mfaService.addNewConnectionToken(user.getUsername());
                    qrCodeManager.addQrCode(token.toString(), user.getUsername());
                } catch (IOException | WriterException exc) {
                    log.error("An error occurred", exc);
                }
            }
            model.addAttribute("token", mfaService.getToken(user.getUsername()).orElse(new Token()));
        }

        model.addAttribute("isIdConfirmed", session.getAttribute("isIdConfirmed") != null);
        session.removeAttribute("isIdConfirmed");
        model.addAttribute("theme", theme);
        return "settings";
    }
}
