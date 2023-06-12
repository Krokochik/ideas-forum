package com.krokochik.ideasForum.rest;

import com.krokochik.ideasForum.controller.AuthController;
import com.krokochik.ideasForum.model.Token;
import com.krokochik.ideasForum.model.User;
import com.krokochik.ideasForum.repository.UserRepository;
import com.krokochik.ideasForum.service.crypto.Cryptographer;
import com.krokochik.ideasForum.service.crypto.TokenService;
import com.krokochik.ideasForum.service.mfa.MFAService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

@RestController
@RequestMapping("mfa")
public class MFAController {

    @Autowired
    MFAService mfaService;

    @Autowired
    TokenService tokenService;

    @Autowired
    UserRepository userRepository;

    @GetMapping(value = "/ping", produces = "application/json")
    public HashMap<String, Object> ping(HttpServletResponse response) {
        response.setStatus(200);
        return new HashMap<>(){{
            put("response", "pong");
        }};
    }

    @GetMapping("/{publicToken}")
    public HashMap<String, Object> mfaConnecting(HttpServletResponse response,
                                                 @PathVariable(name = "publicToken") String publicToken) {
        String mfaToken = "";
        short statusCode = 403;
        try {
            User user = userRepository.findByUsername(
                    AuthController.getContext().getAuthentication().getName());
            Token token = mfaService.getToken(user.getUsername()).orElse(new Token());
            if (token.getPublicPart().equals(publicToken)) {
                mfaToken = tokenService.generateToken();
                userRepository.setMfaTokenById(mfaToken, user.getId());
                mfaToken = Cryptographer.encrypt(mfaToken, "", token.getPrivatePart());
                statusCode = 200;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            statusCode = 500;
        }

        response.setStatus(statusCode);
        String finalMfaToken = mfaToken;
        return new HashMap<>() {{
            put("token", finalMfaToken);
        }};
    }

    @PostMapping("/confirm")
    public HashMap<String, Object> confirmMfaConnecting(HttpServletResponse response,
                                                        @RequestBody HashMap<String, String> requestBody) {
        User user = userRepository.findByUsername(
                AuthController.getContext().getAuthentication().getName());

        String mfaStatus = requestBody.get("mfaStatus");
        try {
            mfaStatus = Cryptographer.decrypt(mfaStatus, "", user.getMfaToken());
        } catch (Exception e) {
            response.setStatus(400);
            if (mfaStatus == null)
                mfaStatus = "";
        }
        if (mfaStatus.equals("connected")) {
            mfaService.removeToken(user.getUsername());

            HashSet<String> resetTokens = new HashSet<>();
            for (int i = 0; i < 10; i++) {
                resetTokens.add(tokenService.generateToken(9L));
            }
            userRepository.setMfaResetTokensById(resetTokens, user.getId());
            userRepository.setMfaConnectedById(true, user.getId());

            User[] users = userRepository.getAllUsers();
            Arrays.stream(users).forEach(User::startMfaCodeGenerating);

            response.setStatus(200);
            return new HashMap<>() {{
                put("mfaResetTokens", resetTokens);
            }};
        }

        if (response.getStatus() != 400)
            response.setStatus(500);

        return new HashMap<>();
    }
}
