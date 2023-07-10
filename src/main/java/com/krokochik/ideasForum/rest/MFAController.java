package com.krokochik.ideasForum.rest;

import com.google.gson.Gson;
import com.krokochik.ideasForum.model.Token;
import com.krokochik.ideasForum.model.User;
import com.krokochik.ideasForum.repository.UserRepository;
import com.krokochik.ideasForum.service.crypto.Cryptographer;
import com.krokochik.ideasForum.service.crypto.TokenService;
import com.krokochik.ideasForum.service.mfa.MFAService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
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
        return new HashMap<>() {{
            put("response", "pong");
        }};
    }

    @PostMapping(value = "/codes", produces = "application/json")
    public HashMap<String, Object> produceMfaCodesToHtml(HttpServletResponse response, Authentication authentication) {
        if (authentication == null) {
            response.setStatus(403);
            return new HashMap<>();
        }
        User user = userRepository.findByUsername(authentication.getName());
        if (user.isMfaConnected()) {
            response.setStatus(200);
            return new HashMap<>() {{
                put("codes", user.getMfaResetTokens());
            }};
        } else {
            response.setStatus(403);
            return new HashMap<>();
        }
    }

    @GetMapping("/{publicToken}")
    public HashMap<String, Object> mfaConnecting(HttpServletResponse response,
                                                 @PathVariable(name = "publicToken") String publicToken) {
        String mfaToken = "";
        User user = new User();
        short statusCode = 403;
        try {
            user = userRepository.findByUsername(mfaService.getUsernameByPublicTokenPart(publicToken).orElse(""));
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
        User finalUser = user;
        return new HashMap<>() {{
            put("token", finalMfaToken);
            put("username", finalUser.getUsername());
        }};
    }

    @PostMapping("/confirm")
    public HashMap<String, Object> confirmMfaConnecting(HttpServletResponse response,
                                                        @RequestBody String requestBodyString) {

        System.out.println(requestBodyString);
        HashMap<String, String> requestBody = new Gson().fromJson(requestBodyString, HashMap.class);
        User user = userRepository.findByUsername(requestBody.get("username"));

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
            for (int i = 0; i < 16; i++) {
                resetTokens.add(tokenService.generateMfaResetCode());
            }

            User newUser = userRepository.findByUsername(user.getUsername());
            newUser.setMfaResetTokens(resetTokens);
            newUser.setMfaConnected(true);
            newUser.setQrcode(null);
            userRepository.save(newUser);

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
