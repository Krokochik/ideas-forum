package com.krokochik.ideasForum.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Controller
public class TerminalController {

    @GetMapping("/terminal")
    public String terminalMapping() {
        return "terminal";
    }

    @RestController
    static class Terminal {
        @PostMapping("/terminal")
        public Map<String, Object> commandsMapping(@RequestBody String requestBodyStr) {
            System.out.println("request");
            System.out.println(requestBodyStr);
            return new HashMap<>() {{
                put("status", "200");
            }};
        }
    }
}
