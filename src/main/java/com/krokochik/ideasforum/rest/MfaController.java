package com.krokochik.ideasforum.rest;

import com.google.gson.Gson;
import com.krokochik.ideasforum.model.db.User;
import com.krokochik.ideasforum.model.service.Token;
import com.krokochik.ideasforum.service.crypto.Cryptographer;
import com.krokochik.ideasforum.service.crypto.TokenService;
import com.krokochik.ideasforum.service.jdbc.UserService;
import com.krokochik.ideasforum.service.mfa.MFAService;
import com.krokochik.ideasforum.service.security.SecurityRoutineProvider;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.crypto.IllegalBlockSizeException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.springframework.security.core.context.SecurityContextHolder.getContext;


@Slf4j
@RestController
@RequestMapping("mfa")
public class MfaController {

    @Autowired
    MFAService mfaService;

    @Autowired
    TokenService tokenService;

    @Autowired
    UserService userService;

    @Autowired
    SecurityRoutineProvider srp;

    /*
     * to connect mfa client must confirm the addition sending
     * encrypted with the private token's part a message
     * to a path that is the public token's part
     */

    @GetMapping(value = "/ping", produces = "application/json")
    public HashMap<String, Object> ping(HttpServletResponse response) {
        response.setStatus(200);
        return new HashMap<>() {{
            put("response", "pong");
        }};
    }

    @PostMapping(value = "/codes", produces = "application/json")
    public HashMap<String, Object> produceMfaCodesToHtml(HttpServletResponse response, Authentication authentication) {
        System.out.println("codes");
        if (authentication == null) {
            response.setStatus(403);
            return new HashMap<>();
        }
        Optional<User> userOptional = userService.findByUsername(authentication.getName());
        User user;
        if (userOptional.isPresent() &&
                ((user = userOptional.get()).isMfaConnecting())) {
            response.setStatus(200);
            user.setMfaConnecting(false);
            userService.update(user);
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
            user = userService.findByUsernameOrUnknown(
                    mfaService.getUsernameByPublicTokenPart(publicToken).orElse(""));
            Token token = mfaService.getToken(user.getUsername()).orElse(new Token());
            if (token.getPublicPart().equals(publicToken)) {
                mfaToken = tokenService.generateToken();
                userService.setMfaTokenById(mfaToken, user.getId());
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
        HashMap<String, String> requestBody = new Gson()
                .fromJson(requestBodyString, HashMap.class);
        AtomicReference<HashMap<String, Object>> responseBody =
                new AtomicReference<>(new HashMap<>());
        Optional<User> userOptional = userService.findByUsername(requestBody.get("username"));

        userOptional.ifPresentOrElse(user -> {
            String mfaStatus = requestBody.get("mfaStatus");
            String mfaToken = user.getMfaToken();
            try {
                mfaStatus = Cryptographer.decrypt(mfaStatus, mfaToken, "");
                System.out.println(mfaStatus);
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

                user = userService.findByUsername(user.getUsername()).orElse(user);
                user.setMfaResetTokens(resetTokens);
                user.setMfaConnecting(true);
                user.setQrcode(null);
                String pin;
                user.setMfaActivatePIN(pin = tokenService.generateMfaPIN());
                userService.update(user);

                response.setStatus(200);
                responseBody.set(new HashMap<>() {{
                    put("PIN", Cryptographer.encrypt(pin, mfaToken, ""));
                }});
            }
        }, () -> response.setStatus(400));

        return responseBody.get();
    }

    @PostMapping("/activated")
    public HashMap<String, Object> isActivated(@RequestParam("username") String username,
                                               @RequestParam("token") String token,
                                               @RequestBody String body,
                                               HttpServletResponse response) {
        System.out.println(body);
        System.out.println(username);
        System.out.println(token);
        int responseStatus;
        HashMap<String, Object> responseBody = new HashMap<>();

        if (username == null || token == null
                || username.isBlank() || token.isBlank()) {
            responseStatus = 400;
        } else if (!userService.exists(username)) {
            responseStatus = 404;
        } else {
            // if the user exists findByUsername will always return !empty
            User user = userService.findByUsername(username).get();
            try {
                token = Cryptographer.decrypt(token, user.getMfaToken(), "");
                if ("token".equals(token)) {
                    responseBody.put("activated", Boolean.toString(user.isMfaActivated()));
                    responseStatus = 200;
                } else {
                    responseStatus = 403;
                }
            } catch (IllegalBlockSizeException e) {
                responseStatus = 403;
            }
        }


        response.setStatus(responseStatus);
        return responseBody;
    }

    @PostMapping("/activate")
    public HashMap<String, Object> activateMfa(@RequestParam("PIN") String pin,
                                               HttpServletResponse response) {
        Optional<User> userOptional = userService.findByUsername(
                getContext().getAuthentication().getName());
        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
        } else {
            response.setStatus(403);
            return new HashMap<>() {{
                put("response", "forbidden");
            }};
        }
        System.out.println(user.getUsername());
        System.out.println(user.getMfaActivatePIN());
        System.out.println(pin);
        if (pin.equals(user.getMfaActivatePIN())) {
            user.setMfaActivated(true);
            user.setMfaActivatePIN(null);
            userService.update(user);
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
