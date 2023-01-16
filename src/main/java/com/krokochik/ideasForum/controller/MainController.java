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
import java.util.Arrays;

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
                           @RequestParam(name = "theme", defaultValue = "", required = false) String theme,
                           @PathVariable(name = "index", required = false) String index) {

        Arrays.stream(postService.fulltextSearch("локалки", PostService.SearchColumns.FULLTEXT_CONTENT, true)).forEach(x -> {
            System.out.println(x.getId());
        });

        try {
            if (Integer.parseInt(index) >= 0) {

            }
        } catch (NumberFormatException | IndexOutOfBoundsException exception) {}

        String tagsExample = "Текст, Шаблон, Пример";
        model.addAttribute("tagsExample", tagsExample);

        model.addAttribute("theme", theme.equals("light") ? "light" : "dark");
        return "main";
    }

    @GetMapping("/add-note")
    public String addNotePage(Model model,  HttpServletRequest request, HttpServletResponse response,
                           @RequestParam(name = "theme", defaultValue = "", required = false) String theme) {

        model.addAttribute("theme", theme.equals("light") ? "light" : "dark");
        return "add-note";
    }
}
