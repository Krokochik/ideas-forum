package com.krokochik.ideasforum.rest;

import com.google.gson.Gson;
import com.krokochik.ideasforum.model.db.User;
import com.krokochik.ideasforum.model.service.Token;
import com.krokochik.ideasforum.repository.UserRepository;
import com.krokochik.ideasforum.service.crypto.Cryptographer;
import com.krokochik.ideasforum.service.crypto.TokenService;
import com.krokochik.ideasforum.service.mfa.MFAService;
import com.krokochik.ideasforum.service.security.SecurityRoutineProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.HashSet;

@Slf4j
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
    SecurityRoutineProvider srp;

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
    public HashMap<String, Object> produceMfaCodesToHtml(HttpServletResponse response, Authentication authentication) {
        if (authentication == null) {
            response.setStatus(403);
            return new HashMap<>();
        }
        User user = userRepository.findByUsername(authentication.getName());
        if (user.isMfaConnecting() || user.isMfaActivated()) {
            response.setStatus(200);
            user.setMfaConnecting(false);
            userRepository.save(user);
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
                mfaToken = Cryptographer.encrypt(mfaToken, token.getPrivatePart(), "");
                statusCode = 200;
            }
        } catch (Exception exc) {
            log.error("An error occurred", exc);
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
            mfaStatus = Cryptographer.decrypt(mfaStatus, user.getMfaToken(), "");
        } catch (Exception e) {
            response.setStatus(400);
            if (mfaStatus == null)
                mfaStatus = "";
        }
        if (mfaStatus.equals("connected")) {
            mfaService.removeToken(user.getUsername());

            HashSet<String> resetTokens = new HashSet<>();
            for (int i = 0; i < 16; i++) {
                String token = tokenService.generateMfaResetCode();
                if (!resetTokens.contains(token))
                    resetTokens.add(token);
                else i--;
            }

            user = userRepository.findByUsername(user.getUsername());
            user.setMfaResetTokens(resetTokens);
            user.setMfaConnecting(true);
            user.setQrcode(null);
            String PIN;
            user.setMfaActivatePIN(PIN = tokenService.generateMfaPIN());
            userRepository.save(user);

            response.setStatus(200);
            return new HashMap<>() {{
                put("PIN", PIN);
            }};
        }

        if (response.getStatus() != 400)
            response.setStatus(500);

        return new HashMap<>();
    }

    @PostMapping("/activate")
    public HashMap<String, Object> activateMfa(@RequestParam("PIN") String PIN, HttpServletResponse response) {
        User user = userRepository.findByUsername(srp.getContext().getAuthentication().getName());
        System.out.println(user.getUsername());
        System.out.println(user.getMfaActivatePIN());
        System.out.println(PIN);
        if (PIN.equals(user.getMfaActivatePIN())) {
            user.setMfaActivated(true);
            user.setMfaActivatePIN(null);
            userRepository.save(user);
            return new HashMap<>() {{
                put("response", "activated");
            }};
        } else {
            response.setStatus(403);
            return new HashMap<>() {{
                put("response", "wrong PIN");
            }};
        }
    }
}
