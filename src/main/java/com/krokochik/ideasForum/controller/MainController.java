package com.krokochik.ideasForum.controller;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.font.GraphicAttribute;
import java.awt.image.ImageObserver;
import java.text.AttributedCharacterIterator;

@Controller
public class MainController {


    @GetMapping({"/main", "/", "", "/main/{index}"})
    public String mainPage(Model model, HttpServletRequest request, HttpServletResponse response,
                           @RequestParam(name = "lang", defaultValue = "", required = false) String language,
                           @RequestParam(name = "theme", defaultValue = "", required = false) String theme,
                           @PathVariable(name = "index", required = false) String index) {

        try {
            if (Integer.parseInt(index) >= 0) {

            }
        } catch (NumberFormatException | IndexOutOfBoundsException exception) {}

        String tagsExample = "Текст, Шаблон, Пример";
        model.addAttribute("tagsExample", tagsExample);
        model.addAttribute("theme", theme.equals("light") ? "light" : "dark");
        model.addAttribute("lang", !language.equals("") ? language : request.getHeader("Accept-Language").substring(0, 2));
        return "main";
    }

    @GetMapping("/add-note")
    public String addNotePage(Model model,  HttpServletRequest request, HttpServletResponse response,
                           @RequestParam(name = "lang", defaultValue = "", required = false) String language,
                           @RequestParam(name = "theme", defaultValue = "", required = false) String theme) {

        model.addAttribute("theme", theme.equals("light") ? "light" : "dark");
        model.addAttribute("lang", !language.equals("") ? language : request.getHeader("Accept-Language").substring(0, 2));
        return "add-note";
    }

    @GetMapping("/settings")
    public String settingsPage(Model model,  HttpServletRequest request, HttpServletResponse response,
                               @RequestParam(name = "lang", defaultValue = "", required = false) String language,
                               @RequestParam(name = "theme", defaultValue = "", required = false) String theme) {

        model.addAttribute("theme", theme.equals("light") ? "light" : "dark");
        model.addAttribute("lang", !language.equals("") ? language : request.getHeader("Accept-Language").substring(0, 2));
        return "settings";
    }
}
