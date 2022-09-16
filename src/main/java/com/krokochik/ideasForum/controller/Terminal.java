package com.krokochik.ideasForum.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class Terminal {

    @PostMapping("/terminal/logic")
    public Map<String, Object> commandsMapping(@RequestBody String requestBodyStr) {
        System.out.println("request");
        System.out.println(requestBodyStr);
        return new HashMap<>() {{
            put("status", "200");
        }};
    }

}
