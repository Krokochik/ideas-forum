package com.krokochik.ideasforum.api.validator;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.concurrent.atomic.AtomicBoolean;

@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExecuteRequestValidator implements RequestValidator<String> {

    String request;
    HttpServletResponse httpResponse;

    @Override
    public String validate(AtomicBoolean result) {
        result.set(false);

        JsonObject responseBody = new JsonObject();
        JsonElement jsonRequest = JsonParser.parseString(request);
        if (jsonRequest.getAsJsonObject().keySet().isEmpty()) {
            httpResponse.setStatus(400);
            responseBody.add("status", new JsonPrimitive("err"));
            responseBody.add("err", new JsonPrimitive("Request body cannot be empty."));
            return responseBody.toString();
        }

        if (!jsonRequest.isJsonObject()) {
            httpResponse.setStatus(400);
            responseBody.add("status", new JsonPrimitive("err"));
            responseBody.add("err", new JsonPrimitive("Unknown request body type."));
            return responseBody.toString();
        }

        JsonObject requestBody = jsonRequest.getAsJsonObject();
        if (!requestBody.has("cmd") || requestBody.get("cmd").isJsonNull()) {
            httpResponse.setStatus(400);
            responseBody.add("status", new JsonPrimitive("err"));
            responseBody.add("err", new JsonPrimitive("Command cannot be null."));
            return responseBody.toString();
        }

        if (!requestBody.has("ctx") || requestBody.get("ctx").isJsonNull()) {
            httpResponse.setStatus(400);
            responseBody.add("status", new JsonPrimitive("err"));
            responseBody.add("err", new JsonPrimitive("Context cannot be null."));
            return responseBody.toString();
        }

        if (!requestBody.get("cmd").isJsonPrimitive()) {
            httpResponse.setStatus(400);
            responseBody.add("status", new JsonPrimitive("err"));
            responseBody.add("err", new JsonPrimitive("Bad command syntax: primitive was expected."));
            return responseBody.toString();
        }

        if (!requestBody.get("ctx").isJsonObject()) {
            httpResponse.setStatus(400);
            responseBody.add("status", new JsonPrimitive("err"));
            responseBody.add("err", new JsonPrimitive("Bad context syntax: object was expected."));
            return responseBody.toString();
        }

        result.set(true);
        return null;
    }
}
