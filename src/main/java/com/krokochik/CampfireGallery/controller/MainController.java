package com.krokochik.CampfireGallery.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {


    @GetMapping({"/main", "/", ""})
    public String mainPage(Model model) {
        return "main";
    }

    @GetMapping("/add-note")
    public String addNotePage(Model model) {
        return "add-note";
    }
}
