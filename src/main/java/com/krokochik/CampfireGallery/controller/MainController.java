package com.krokochik.CampfireGallery.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

@Controller
public class MainController {


    @GetMapping({"/main", "/", ""})
    public String mainPage(Model model,  HttpServletRequest request, @RequestParam(name = "lang", defaultValue = "", required = false) String language) {
        model.addAttribute("lang", !language.equals("") ? language : request.getHeader("Accept-Language").substring(0, 2));
        return "main";
    }

    @GetMapping("/add-note")
    public String addNotePage(Model model,  HttpServletRequest request, @RequestParam(name = "lang", defaultValue = "", required = false) String language) {
        model.addAttribute("lang", !language.equals("") ? language : request.getHeader("Accept-Language").substring(0, 2));
        return "add-note";
    }
}
