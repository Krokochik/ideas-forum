package com.krokochik.ideasForum.rest;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.krokochik.ideasForum.controller.AuthController;
import com.krokochik.ideasForum.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@RestController
public class RequestsController {

    @Autowired
    UserRepository userRepository;

    @ResponseBody
    @CrossOrigin(originPatterns = "https://ideas-forum.herokuapp.com/**")
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
            } catch (Exception ignored) {
            }

            username = requestBody.get("username").getAsString();
            nickname = requestBody.get("nickname").getAsString();
        } catch (Exception exception) {
            statusCode = 500;
            exception.printStackTrace();
        }

        if (username.equalsIgnoreCase(AuthController.getContext().getAuthentication().getName())) {
            if (!avatar.equals("")) {

                final double BYTES_IN_MEGABYTE = 1e+6;
                final double INFELICITY_COEFFICIENT = 1.402;
                final double MAX_AVATAR_WEIGHT = 5.0;

                if ((avatar.length() / BYTES_IN_MEGABYTE / INFELICITY_COEFFICIENT) <= MAX_AVATAR_WEIGHT) {
                    userRepository.setAvatarById(avatar.getBytes(), userRepository.findByUsername(username).getId());
                    statusCode = 200;
                } else {
                    statusCode = 400;
                    message = "Avatar is too hard.";
                }
            } else {
                statusCode = 400;
                message = "Avatar is null.";
            }

            if (!nickname.isEmpty() && nickname.length() >= 4) {
                userRepository.setNicknameById(nickname, userRepository.findByUsername(username).getId());
                statusCode = 200;
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