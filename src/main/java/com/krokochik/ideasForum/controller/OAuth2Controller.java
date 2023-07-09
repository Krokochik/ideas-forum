package com.krokochik.ideasForum.controller;

import com.krokochik.ideasForum.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class OAuth2Controller {

    @Autowired
    UserRepository userRepository;

    @GetMapping("/oauth2/{status}")
    @ResponseBody
    public String oauth2finished(OAuth2AuthenticationToken authentication, @PathVariable(name = "status") String status) {
        OAuth2User user = authentication.getPrincipal();
        switch (status) {
            case "success":
                if (user.getAttribute("id") != null) {
                    if (userRepository.getUserByOAuth2Id(((Integer) user.getAttribute("id")).longValue()) != null)

                    break;
                }
            case "failure":
                return "redirect:/login";
        }
        return "hello world";
    }
}
