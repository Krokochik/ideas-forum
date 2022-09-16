package com.krokochik.ideasForum.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
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
