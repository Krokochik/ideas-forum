package com.krokochik.ideasForum.controller;

import com.krokochik.ideasForum.model.User;
import com.krokochik.ideasForum.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    @Autowired
    UserRepository userRepository;

    @GetMapping("/sign-up")
    public String loginPageGet() {
        return "";
    }

    @PostMapping("/sign-up")
    public String loginPage(@ModelAttribute(name = "logname") String name, @ModelAttribute(name = "logemail") String email, @ModelAttribute(name = "logpass") String pass) {
        User user;
        if (name.equals(""))
            user = new User(name, email, pass);
        else user = new User(email, pass);

        if (userRepository.findByUsername(user.getUsername()) == null) {
            userRepository.save(user);
        }

        else System.out.println(":(");

        return "";
    }
}
