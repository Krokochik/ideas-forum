package com.krokochik.ideasforum.rest;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.krokochik.ideasforum.model.db.User;
import com.krokochik.ideasforum.service.jdbc.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("api")
public class API {
    @Autowired
    UserService userService;

    /* * example request
        {
            "cmd": "get", // required
            "ctx": {      // required
                "username": "root"
            },
            "param": [
                "nickname"
            ]
        }
     * * response
        {
            "nickname": "nick",
            "status": "ok" // status is ok or err.
        }                  // If err, must be present err element with error description
     */
    @PostMapping("/exec")
    public Map<String, Object> execute(@RequestBody String request,
                                       HttpServletResponse httpResponse) {
        Map<String, Object> responseBody = new HashMap<>();

        JsonElement jsonRequest = JsonParser.parseString(request);
        if (jsonRequest.isJsonNull()) {
            httpResponse.setStatus(400);
            responseBody.put("status", "err");
            responseBody.put("err", "Request body cannot be json-null.");
            return responseBody;
        }

        if (!jsonRequest.isJsonObject()) {
            httpResponse.setStatus(400);
            responseBody.put("status", "err");
            responseBody.put("err", "Unknown request body type.");
            return responseBody;
        }

        JsonObject requestBody = jsonRequest.getAsJsonObject();
        if (!requestBody.has("cmd") || requestBody.get("cmd").isJsonNull()) {
            httpResponse.setStatus(400);
            responseBody.put("status", "err");
            responseBody.put("err", "Command cannot be null.");
            return responseBody;
        }

        if (!requestBody.has("ctx") || requestBody.get("ctx").isJsonNull()) {
            httpResponse.setStatus(400);
            responseBody.put("status", "err");
            responseBody.put("err", "Context cannot be null.");
            return responseBody;
        }

        if (!requestBody.get("cmd").isJsonPrimitive()) {
            httpResponse.setStatus(400);
            responseBody.put("status", "err");
            responseBody.put("err", "Bad command syntax: primitive was expected.");
            return responseBody;
        }

        if (!requestBody.get("ctx").isJsonObject()) {
            httpResponse.setStatus(400);
            responseBody.put("status", "err");
            responseBody.put("err", "Bad context syntax: object was expected.");
            return responseBody;
        }

        String command = requestBody.get("cmd").getAsString();
        JsonObject context = requestBody.get("ctx").getAsJsonObject();

        switch (command) {
            case "get":
                if (!requestBody.has("param")) {
                    httpResponse.setStatus(400);
                    responseBody.put("status", "err");
                    responseBody.put("err", "Parameters are required for get.");
                    return responseBody;
                }

                if (!requestBody.get("param").isJsonArray()) {
                    httpResponse.setStatus(400);
                    responseBody.put("status", "err");
                    responseBody.put("err", "Bad 'param' syntax: array was expected.");
                    return responseBody;
                }

                httpResponse.setStatus(
                        executeGet(context, requestBody.get("param").getAsJsonArray(), responseBody)
                );
                return responseBody;
            default:
                httpResponse.setStatus(404);
                responseBody.put("status", "err");
                responseBody.put("err", String.format("Unknown command: '%s'.", command));
                return responseBody;
        }
    }

    private short executeGet(JsonObject context, JsonArray parameters, Map<String, Object> responseBody) {
        if (!context.has("username")) {
            responseBody.put("status", "err");
            responseBody.put("err", "Username is required in context of get.");
            return 400;
        }

        if (context.get("username").isJsonNull()) {
            responseBody.put("status", "err");
            responseBody.put("err", "Username cannot be null.");
            return 400;
        }

        if (!context.get("username").isJsonPrimitive()) {
            responseBody.put("status", "err");
            responseBody.put("err", "Bad username.");
            return 400;
        }

        String username = context.get("username").getAsString();
        if (username.isBlank()) {
            responseBody.put("status", "err");
            responseBody.put("err", "Username cannot be blank.");
            return 400;
        }

        Optional<User> user = userService.findByUsername(username);
        if (user.isEmpty()) {
            responseBody.put("status", "err");
            responseBody.put("err", String.format(
                    "Couldn't find a user with this username: '%s'.", username));
            return 404;
        }

        if (parameters.isEmpty()) {
            responseBody.put("status", "ok");
            return 200;
        }

        Set<String> iterated = new HashSet<>();
        for (JsonElement element : parameters) {
            String parameter = element.getAsString();
            if (!iterated.contains(parameter)) {
                iterated.add(parameter);
                switch (parameter) {
                    case "nickname":
                        responseBody.put("nickname", user.get().getNickname());
                        break;
                    case "avatar":
                        if (Arrays.equals(user.get().getAvatar(), new User().getAvatar())) {
                            responseBody.put("avatar", "guest");
                        } else {
                            if (user.get().getAvatar() == null) {
                                responseBody.put("avatar", "null");
                            } else {
                                responseBody.put("avatar", new String(user.get().getAvatar()));
                            }
                        }
                        break;
                }
            }
        }
        responseBody.put("status", "ok");
        return 200;
    }
}
