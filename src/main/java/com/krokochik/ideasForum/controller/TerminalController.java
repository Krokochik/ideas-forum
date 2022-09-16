package com.krokochik.ideasForum.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TerminalController {

    @GetMapping("/terminal")
    public String terminalMapping() {
        return "terminal";
    }

}
