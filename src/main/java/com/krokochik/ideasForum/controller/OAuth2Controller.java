package com.krokochik.ideasForum.controller;

import com.krokochik.ideasForum.model.User;
import com.krokochik.ideasForum.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @ResponseBody
    @GetMapping("/oauth2/{status}")
    public String oauth2finished(OAuth2AuthenticationToken authentication, @PathVariable(name = "status") String status) {
        OAuth2User oAuth2User = authentication.getPrincipal();
        switch (status) {
            case "success":
                if (oAuth2User.getAttribute("id") != null) {
                    Long id;
                    try {
                        id = ((Integer) oAuth2User.getAttribute("id")).longValue();
                    } catch (Exception exception) {
                        id = -1L;
                    }
                    User user;
                    if ((user = userRepository.getUserByOAuth2Id(id)) != null)
                        AuthController.authorizeUser(SecurityContextHolder.getContext(), user);
                    else {
                        oAuth2User.getAttributes().forEach((s, o) -> System.out.println(s + ": " + o));
                    }
                    break;
                }
            case "failure":
                return "redirect:/login";
        }
        return "hello world";
    }
}
