package com.krokochik.ideasForum.controller;

import com.krokochik.ideasForum.repository.PostRepository;
import com.krokochik.ideasForum.repository.UserRepository;
import com.krokochik.ideasForum.service.MailService;
import com.krokochik.ideasForum.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class MainController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    MailService mailService;

    @Autowired
    PostService postService;

    @Autowired
    PostRepository postRepository;

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
}
