package com.krokochik.ideasForum.controller.pages;

import com.google.zxing.WriterException;
import com.krokochik.ideasForum.model.db.User;
import com.krokochik.ideasForum.model.service.Token;
import com.krokochik.ideasForum.repository.UserRepository;
import com.krokochik.ideasForum.service.MailService;
import com.krokochik.ideasForum.service.mfa.MFAService;
import com.krokochik.ideasForum.service.mfa.QRCodeManager;
import com.krokochik.ideasForum.service.security.SecurityRoutineProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpSession;
import java.io.IOException;

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

    @Autowired
    SecurityRoutineProvider srp;

    @GetMapping("/settings")
    public String settings(Model model, HttpSession session,
                               @CookieValue(name = "theme", required = false, defaultValue = "dark") String theme) {

        boolean isMfaProcessingAtSession = session.getAttribute("mfa-reset-tokens") != null;


        if (srp.isAuthenticated() ) {
            User user = userRepository.findByUsername(srp.getContext().getAuthentication().getName());
            if ((user.getQrcode() == null ||
                    !qrCodeManager.hasUserQrCode(user.getUsername()) ||
                    !isMfaProcessingAtSession ||
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
}
