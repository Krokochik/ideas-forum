package com.krokochik.ideasforum.api.controller;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.krokochik.ideasforum.api.annotation.Validator;
import com.krokochik.ideasforum.api.service.ExecuteRequestService;
import com.krokochik.ideasforum.api.validator.ExecuteRequestValidator;
import com.krokochik.ideasforum.service.jdbc.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/exec")
public class ExecuteRequestController {

    @Autowired
    UserService userService;

    @Autowired
    ExecuteRequestService requestService;

    @PostMapping(produces = "application/json")
    @Validator(ExecuteRequestValidator.class)
    public String execute(@RequestBody String request,
                          HttpServletResponse httpResponse) {

        JsonObject responseBody = new JsonObject();
        JsonElement jsonRequest = JsonParser.parseString(request);
        JsonObject requestBody = jsonRequest.getAsJsonObject();

        String command = requestBody.get("cmd").getAsString();
        JsonObject context = requestBody.get("ctx").getAsJsonObject();

        switch (command) {
            case "get":
                httpResponse.setStatus(
                        requestService.executeGet(context,
                                requestBody.get("param").getAsJsonArray(),
                                responseBody, null)
                );
                return responseBody.toString();
            default:
                httpResponse.setStatus(404);
                responseBody.add("status", new JsonPrimitive("err"));
                responseBody.add("err", new JsonPrimitive(String.format(
                        "Unknown command: '%s'.", command)));
                return responseBody.toString();
        }
    }
}
