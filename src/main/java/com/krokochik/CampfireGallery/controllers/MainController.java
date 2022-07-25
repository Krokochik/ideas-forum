package com.krokochik.CampfireGallery.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {
    @GetMapping("/")
    public String mainPage(Model model) {
        return "main";
    }
    @GetMapping("/repositories/1")
    public String test(Model model) {
        return "main";
    }
}
