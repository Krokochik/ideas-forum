package com.krokochik.ideasForum.controller;

import com.google.gson.JsonObject;
import com.krokochik.ideasForum.model.Role;
import com.krokochik.ideasForum.model.User;
import com.krokochik.ideasForum.repository.UserRepository;
import org.apache.tomcat.util.json.JSONParser;
import org.apache.tomcat.util.json.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

@RestController
public class TerminalLogicController {

    @Autowired
    UserRepository userRepository;

    @PostMapping("/terminal/logic")
    public Map<String, Object> commandsMapping(@RequestBody String requestBodyStr, HttpServletResponse response) {
        JsonObject requestBody;
        String command = "";
        String message = "";
        short statusCode = 200;

        try {
            requestBody = (JsonObject) new JSONParser(requestBodyStr).parse();
            command = requestBody.get("cmd").getAsString().replaceAll(" ", "").toLowerCase();
        } catch (ParseException parseException) {
            statusCode = 500;
        }

        switch (command.substring(0, 3)) {
            case "add" -> {
                Map<Boolean, String> result = add(command.substring(3));
                if (result.containsKey(true)) {
                    statusCode = 201;
                    message = result.get(true);
                }
                else {
                    statusCode = Short.parseShort(result.get(false).substring(0, 3));
                    message = result.get(false).substring(3);
                }
            }
        }

        response.setStatus(statusCode);
        String finalMessage = message;
        return new HashMap<>() {{
            put("msg", finalMessage);
        }};
    }

    private Map<Boolean, String> add(String command) {
        try {
            switch (command.substring(0, command.indexOf(":"))) {
                case "user" -> {
                    command = command.substring(command.indexOf(":") + 1);
                    HashMap<String, String> args = new HashMap<>();
                    for (String arg : command.split(",")) {
                        if (arg.contains("="))
                            args.put(arg.split("=")[0], arg.split("=")[1]);
                        else
                            return new HashMap<>() {{
                                put(false, "400Could not be found param value with name '" + arg + "'");
                            }};
                    }
                    if (args.containsKey("pass") && args.containsKey("nick") && args.containsKey("email")) {
                        User user = new User(args.get("nick"), args.get("email"), args.get("pass"));
                        if (args.containsKey("roles")) {
                            String[] roles = args.get("roles").contains("&") ? args.get("roles").split("&") : new String[]{args.get("roles")};
                            if (!roles[0].equals("null")) {
                                HashSet<Role> roleHashSet = new HashSet<>();
                                try {
                                    Arrays.stream(roles).forEach(str -> roleHashSet.add(Role.valueOf(str)));
                                } catch (IllegalArgumentException exception) {
                                    return new HashMap<>() {{
                                        put(false, "400Unknown field in role list'");
                                    }};
                                }
                                user.setRoles(roleHashSet);
                            }
                        } else {
                            user.setRoles(Collections.singleton(Role.USER));
                        }
                        try {
                            userRepository.save(user);
                            Set<String> roles = new HashSet<>();
                            if (!user.getRoles().isEmpty())
                                for (Role role : user.getRoles()) {
                                    roles.add(role.toString());
                                }
                            else
                                roles.add("null");
                            return new HashMap<>() {{
                                put(true, String.format("Created user with nickname '%s', email '%s', password '%s' and roles: '%s'",
                                        user.getUsername(), user.getEmail(), user.getPassword(), String.join(", ", roles)));
                            }};

                        } catch (Exception exception) {
                            return new HashMap<>() {{
                                put(false, "500Unknown internal error");
                            }};
                        }
                    }
                }
                default -> {
                    String finalCommand = command;
                    return new HashMap<>() {{
                        put(false, "400Unknown param name: " + finalCommand.substring(0, finalCommand.indexOf(":")));
                    }};
                }
            }
        } catch (Exception exception) {
            return new HashMap<>() {{
                put(false, "400Expected ':' after param name");
            }};
        }
        return new HashMap<>() {{
            put(false, "500Unknown internal error");
        }};
    }

}