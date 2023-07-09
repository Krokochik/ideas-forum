package com.krokochik.ideasForum.rest;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.krokochik.ideasForum.controller.SecurityController;
import com.krokochik.ideasForum.model.User;
import com.krokochik.ideasForum.repository.UserRepository;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
public class ProfileController {

    @Autowired
    UserRepository userRepository;

    @GetMapping(value = "/avatar")
    public void avatar(HttpServletResponse response) throws IOException {
        byte[] decodedAvatar = null;
        if (SecurityController.isAuthenticated()) {
            User user = userRepository.findByUsername(SecurityController.getContext().getAuthentication().getName());
            if (new String(user.getAvatar()).endsWith("==")) {
                decodedAvatar = Base64.decodeBase64(user.getAvatar());
            }
            else {
                response.sendRedirect(new String(user.getAvatar()));
            }
        } else decodedAvatar = Base64.decodeBase64(new User().getAvatar());
        if (decodedAvatar != null) {
            response.setContentType(MediaType.IMAGE_JPEG_VALUE);
            response.getOutputStream().write(decodedAvatar);
        }
    }

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
                avatar = avatar.replaceFirst("^data:image\\/[^;]+;base64,", "");
            } catch (Exception ignored) { }

            username = requestBody.get("username").getAsString();
            nickname = requestBody.get("nickname").getAsString();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        if (username.equalsIgnoreCase(SecurityController.getContext().getAuthentication().getName())) {
            if (!avatar.equals("")) {

                final double BYTES_IN_MEGABYTE = 1e+6;
                final double INFELICITY_COEFFICIENT = 1.402;
                final double MAX_AVATAR_WEIGHT = 5.0;

                if ((avatar.length() / BYTES_IN_MEGABYTE / INFELICITY_COEFFICIENT) <= MAX_AVATAR_WEIGHT) {
                    userRepository.setAvatarById(avatar.getBytes(), userRepository.findByUsername(username).getId());
                } else {
                    message = "Avatar is too heavy.";
                }
            } else {
                message = "Avatar is null.";
            }

            if (!nickname.isEmpty() && nickname.length() >= 4) {
                userRepository.setNicknameById(nickname, userRepository.findByUsername(username).getId());
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