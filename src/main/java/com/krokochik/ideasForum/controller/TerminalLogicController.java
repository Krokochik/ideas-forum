package com.krokochik.ideasForum.controller;

import com.google.gson.JsonObject;
import com.krokochik.ideasForum.model.User;
import com.krokochik.ideasForum.repository.UserRepository;
import org.apache.tomcat.util.json.JSONParser;
import org.apache.tomcat.util.json.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@RestController
public class TerminalLogicController {

    @Autowired
    UserRepository userRepository;

    @PostMapping("/terminal/logic")
    public Map<String, Object> commandsMapping(@RequestBody String requestBodyStr, HttpServletResponse response) {
        JsonObject requestBody;
        String command = "";
        short statusCode = 200;

        try {
            requestBody = (JsonObject) new JSONParser(requestBodyStr).parse();
            command = requestBody.get("cmd").getAsString();
        } catch (ParseException parseException) {
            statusCode = 500;
        }

        switch (command.substring(0, command.indexOf(" ") - 1)) {
            case "add" -> add(command.substring(command.indexOf(" ") + 1));
        }

        response.setStatus(statusCode);
        return new HashMap<>() {{
            put("status", "200");
        }};
    }

    private String add(String command) {
        switch (command.substring(0, command.indexOf(": ") - 1)) {
            case "user" -> {
                command = command.substring(command.indexOf(": ") + 2).replaceAll(" ", "");
                if (command.contains(",pass=") && command.contains(",nick=") && command.contains(",email=")) {
                    userRepository.save(new User(command.substring(command.indexOf(",nick=") + ",nick=".length(), command.indexOf(",") - 1), "", ""));
                }
            }
        }
        return "";
    }

}
