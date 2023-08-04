package com.krokochik.ideasforum.controller.pages;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class MainController {

    @GetMapping({"/main", "/", "", "/main/{index}"})
    public String mainPage(Model model, HttpServletRequest request, HttpServletResponse response,
                           @CookieValue(name = "theme", required = false, defaultValue = "dark") String theme,
                           @PathVariable(name = "index", required = false) String index) {

        try {
            if (Integer.parseInt(index) >= 0) {

            }
        } catch (NumberFormatException | IndexOutOfBoundsException exception) {
        }

        String tagsExample = "Текст, Шаблон, Пример";
        model.addAttribute("tagsExample", tagsExample);

        model.addAttribute("theme", theme);
        return "main.html";
    }

    @GetMapping("/add-note")
    public String addNotePage(Model model,  HttpServletRequest request, HttpServletResponse response,
                              @CookieValue(name = "theme", required = false, defaultValue = "dark") String theme) {

        model.addAttribute("theme", theme);
        return "add-post";
    }
}
