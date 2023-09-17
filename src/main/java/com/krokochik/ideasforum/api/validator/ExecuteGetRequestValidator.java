package com.krokochik.ideasforum.api.validator;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.concurrent.atomic.AtomicBoolean;

@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExecuteGetRequestValidator implements RequestValidator<Integer> {

    JsonObject context;
    JsonArray parameters;
    JsonObject responseBody;

    @Override
    public Integer validate(AtomicBoolean result) {
        result.set(false);

        if (!context.has("username")) {
            responseBody.add("status", new JsonPrimitive("err"));
            responseBody.add("err", new JsonPrimitive("Username is required in context of get."));
            return 400;
        }

        if (context.get("username").isJsonNull()) {
            responseBody.add("status", new JsonPrimitive("err"));
            responseBody.add("err", new JsonPrimitive("Username cannot be null."));
            return 400;
        }

        if (!context.get("username").isJsonPrimitive()) {
            responseBody.add("status", new JsonPrimitive("err"));
            responseBody.add("err", new JsonPrimitive("Bad username."));
            return 400;
        }

        String username = context.get("username").getAsString();
        if (username.isBlank()) {
            responseBody.add("status", new JsonPrimitive("err"));
            responseBody.add("err", new JsonPrimitive("Username cannot be blank."));
            return 400;
        }

        if (parameters.isEmpty()) {
            responseBody.add("status", new JsonPrimitive("ok"));
            return 200;
        }

        result.set(true);
        return 0;
    }
}
