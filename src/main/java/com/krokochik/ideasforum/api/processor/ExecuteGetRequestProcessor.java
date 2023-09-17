package com.krokochik.ideasforum.api.processor;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.krokochik.ideasforum.api.annotation.FromParent;
import com.krokochik.ideasforum.model.db.User;
import com.krokochik.ideasforum.service.crypto.Cryptographer;
import com.krokochik.ideasforum.service.jdbc.UserService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.codec.binary.Base64;

import java.util.Arrays;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteGetRequestProcessor implements RequestProcessor {

    @FromParent
    UserService userService;

    JsonObject context;
    JsonArray parameters;
    JsonObject responseBody;

    public int process() {
        try {
            String username = context.get("username").getAsString(); // checked at controller
            User user = userService.findByUsername(username).get(); // checked at controller

            JsonArray warnings = new JsonArray();
            String mode = "obj";
            if (context.get("mode") != null) {
                if (context.get("mode").getAsString().equals("hash")) {
                    mode = "hash";
                } else if (!context.get("mode").getAsString().equals("obj")) {
                    warnings.add(String.format(
                            "Unknown mode: '%s'. Selected default '%s'.",
                            context.get("mode").getAsString(), mode)
                    );
                }
            }

            Set<String> iterated = new HashSet<>();
            for (JsonElement element : parameters) {
                String parameter = element.getAsString();
                if (!iterated.contains(parameter)) {
                    iterated.add(parameter);
                    switch (parameter) {
                        case "nickname":
                            String nickname = user.getNickname();
                            if (mode.equals("obj")) {
                                responseBody.add("nickname", new JsonPrimitive(nickname));
                            } else if (mode.equals("hash")) {
                                responseBody.add("nickname", new JsonPrimitive(
                                        Cryptographer.getHash(nickname)
                                ));
                            }
                            break;
                        case "avatar":
                            byte[] avatar = user.getAvatar();
                            if (mode.equals("obj")) {
                                if (avatar == null) {
                                    responseBody.add("avatar", null);
                                } else {
                                    responseBody.add("avatar", new JsonPrimitive(
                                            Base64.encodeBase64String(avatar)));
                                }
                            } else if (mode.equals("hash")) {
                                if (Arrays.equals(avatar, new User().getAvatar())) {
                                    responseBody.add("avatar", new JsonPrimitive("guest"));
                                } else if (avatar == null) {
                                    responseBody.add("avatar", new JsonPrimitive("null"));
                                } else {
                                    responseBody.add("avatar", new JsonPrimitive(
                                            Cryptographer.getHash(avatar)));
                                }
                            }
                            break;
                        default:
                            warnings.add(String.format("Unknown parameter: '%s'.", parameter));
                    }
                }
            }
            responseBody.add("status", new JsonPrimitive("ok"));
            if (!warnings.isEmpty()) {
                responseBody.add("warn", warnings);
            }
            return 200;
        } catch (NullPointerException | NoSuchElementException e) {
            throw new RuntimeException(e);
        }
    }
}
