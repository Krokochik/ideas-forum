package com.krokochik.ideasforum.rest;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.krokochik.ideasforum.model.db.User;
import com.krokochik.ideasforum.service.jdbc.UserService;
import com.krokochik.ideasforum.service.security.SecurityRoutineProvider;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.shaded.com.ongres.scram.common.bouncycastle.base64.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import static org.springframework.security.core.context.SecurityContextHolder.getContext;

@Slf4j
@RestController
public class ProfileController {

    @Autowired
    UserService userService;

    @Autowired
    SecurityRoutineProvider srp;

    @PostMapping(value = "/profile", produces = "application/json")
    public Map<String, String> profile(@RequestBody String requestBodyStr, HttpServletResponse response) {
        JsonObject requestBody;
        JsonParser jsonParser = new JsonParser();

        String avatar = "";
        String nickname = "", username = "", message = "";
        short statusCode = 200;

        try {
            requestBody = jsonParser.parse(requestBodyStr).getAsJsonObject();
            try {
                avatar = requestBody.get("avatar").getAsString();
                avatar = avatar.replaceFirst("^data:image/[^;]+;base64,", "");
            } catch (Exception ignored) { }

            username = requestBody.get("username").getAsString();
            nickname = requestBody.get("nickname").getAsString();
        } catch (Exception exc) {
            log.error("An error occurred", exc);
        }

        Optional<User> user = userService.findByUsername(username);
        if (user.isPresent() &&
                username.equalsIgnoreCase(getContext().getAuthentication().getName())) {
            if (!avatar.isBlank()) {

                final double BYTES_IN_MEGABYTE = 1e+6;
                final double INFELICITY_COEFFICIENT = 1.402;
                final double MAX_AVATAR_WEIGHT = 5.0;

                if ((avatar.length() / BYTES_IN_MEGABYTE / INFELICITY_COEFFICIENT) <= MAX_AVATAR_WEIGHT) {
                    userService.setAvatarById(Base64.decode(avatar), user.get().getId());
                } else {
                    message = "Avatar is too heavy.";
                }
            } else {
                message = "Avatar is null.";
            }

            if (!nickname.isBlank() && nickname.length() >= 4) {
                userService.setNicknameById(nickname, user.get().getId());
            } else {
                statusCode = 400;
                message = "New name is null.";
            }
        } else statusCode = 403;

        response.setStatus(statusCode);
        String finalMessage = message;
        return new HashMap<>() {{
            put("msg", finalMessage);
        }};
    }

}