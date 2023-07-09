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

import javax.servlet.http.HttpSession;

@Controller
public class OAuth2Controller {

    @Autowired
    UserRepository userRepository;

    @GetMapping("/oauth2/{status}")
    public String oauth2finished(OAuth2AuthenticationToken authentication, HttpSession session,
                                 @PathVariable(name = "status") String status) {
        OAuth2User oauth2User;
        try {
            oauth2User = authentication.getPrincipal();
        } catch (NullPointerException exception) {
            return "redirect:/login";
        }
        if ("success".equals(status) && oauth2User.getAttribute("id") != null) {
            long id;
            try {
                id = ((Integer) oauth2User.getAttribute("id")).longValue();
            } catch (NullPointerException exception) {
                id = -1L;
            }

            User user;
            if ((user = userRepository.getUserByOAuth2Id(id)) != null) {
                SecurityController.authorizeUser(SecurityContextHolder.getContext(), user);
                return "redirect:/mail-confirm";
            } else {
                // attributes to autofill at sign up
                session.setAttribute("oauth2Login", oauth2User.getAttribute("login"));
                session.setAttribute("oauth2Avatar", oauth2User.getAttribute("avatar_url"));
                oauth2User.getAttributes().forEach((s, o) -> System.out.println(s + ": " + o));

                //  to clear able previous autofill data
                session.setAttribute("authData", null);
                return "redirect:/sign-up?oauth2=true";
            }

        } else return "redirect:/login";
    }
}
