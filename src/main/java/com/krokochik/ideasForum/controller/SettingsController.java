package com.krokochik.ideasForum.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class SettingsController {
    @GetMapping("/settings")
    public String settingsPage(Model model, HttpServletRequest request, HttpServletResponse response,
                               @RequestParam(name = "lang", defaultValue = "", required = false) String language,
                               @RequestParam(name = "theme", defaultValue = "", required = false) String theme) {

        model.addAttribute("auth", AuthController.isAuthenticated());
        model.addAttribute("theme", theme.equals("light") ? "light" : "dark");
        model.addAttribute("lang", !language.equals("") ? language : request.getHeader("Accept-Language").substring(0, 2));
        return "settings";
    }
}