package com.krokochik.ideasForum.rest;

import com.google.gson.Gson;
import com.krokochik.ideasForum.model.db.User;
import com.krokochik.ideasForum.model.service.Token;
import com.krokochik.ideasForum.repository.UserRepository;
import com.krokochik.ideasForum.service.crypto.Cryptographer;
import com.krokochik.ideasForum.service.crypto.TokenService;
import com.krokochik.ideasForum.service.jdbc.UserService;
import com.krokochik.ideasForum.service.mfa.MFAService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("mfa")
public class MFAController {

    @Autowired
    MFAService mfaService;

    @Autowired
    TokenService tokenService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;

    /*
    * to connect mfa client must confirm the addition sending
    * encrypted with the private token's part a message
    * to a path that is the public token's part
    * */

    @GetMapping(value = "/ping", produces = "application/json")
    public HashMap<String, Object> ping(HttpServletResponse response) {
        response.setStatus(200);
        return new HashMap<>() {{
            put("response", "pong");
        }};
    }

    @PostMapping(value = "/codes", produces = "application/json")
    public HashMap<String, Object> produceMfaCodesToHtml(HttpServletResponse response, HttpSession session, Authentication authentication) {
        if (authentication == null) {
            response.setStatus(403);
            return new HashMap<>();
        }
        User user = userRepository.findByUsername(authentication.getName());
        Set<String> tokens = (Set) session.getAttribute("mfa-reset-tokens");
        if (user.isMfaConnected()) {
            response.setStatus(200);
            return new HashMap<>() {{
                put("codes", tokens);
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
                                                        HttpSession session,
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

            user = userRepository.findByUsername(user.getUsername());
            user.setMfaConnected(true);
            user.setQrcode(null);
            userRepository.save(user);
            session.setAttribute("mfa-reset-tokens", resetTokens);

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
