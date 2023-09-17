package com.krokochik.ideasforum.api.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.krokochik.ideasforum.api.annotation.Processor;
import com.krokochik.ideasforum.api.annotation.Validator;
import com.krokochik.ideasforum.api.processor.ExecuteGetRequestProcessor;
import com.krokochik.ideasforum.api.processor.RequestProcessor;
import com.krokochik.ideasforum.api.validator.ExecuteGetRequestValidator;
import com.krokochik.ideasforum.model.db.User;
import com.krokochik.ideasforum.service.jdbc.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ExecuteRequestService {

    @Autowired
    UserService userService;

    @Validator(ExecuteGetRequestValidator.class)
    @Processor(ExecuteGetRequestProcessor.class)
    public int executeGet(JsonObject context, JsonArray parameters,
                          JsonObject responseBody, RequestProcessor processor) {
        String username = context.get("username").getAsString();
        Optional<User> user = userService.findByUsername(username);
        if (user.isEmpty()) {
            responseBody.add("status", new JsonPrimitive("err"));
            responseBody.add("err", new JsonPrimitive(String.format(
                    "Couldn't find a user with this username: '%s'.", username)));
            return 404;
        }

        return processor.process();
    }
}
