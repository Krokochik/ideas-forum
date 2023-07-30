package com.krokochik.ideasForum.controller.auth;

import com.krokochik.ideasForum.model.db.User;
import com.krokochik.ideasForum.repository.UserRepository;
import com.krokochik.ideasForum.service.security.SecurityRoutineProvider;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.net.MalformedURLException;
import java.net.URL;

@Controller
public class OAuth2Controller {

    @Autowired
    UserRepository userRepository;

    @Autowired
    SecurityRoutineProvider srp;

    @SneakyThrows
    @GetMapping("/oauth2/{status}")
    public String oauth2finished(OAuth2AuthenticationToken authentication,
                                 HttpSession session, HttpServletRequest request,
                                 HttpServletResponse response,
                                 @PathVariable(name = "status") String status) {
        OAuth2User oauth2User;
        try {
            oauth2User = authentication.getPrincipal();
        } catch (NullPointerException exception) {
            return "redirect:/login";
        }

        if (status.equals("success") && oauth2User.getAttribute("id") != null) {
            String id;
            try {
                id = oauth2User.getAttribute("id");
            } catch (NullPointerException e) {
                return "redirect:/login";
            }  catch (ClassCastException e) {
                id = ((Integer) oauth2User.getAttribute("id")) + "";
            }

            User user;
            if ((user = userRepository.getUserByOAuth2Id(id)) != null) {
                srp.authorizeUser(SecurityContextHolder.getContext(), user, true, request, response);
                return "redirect:/email-validity-confirmation";
            } else {
                URL avatarUrl = new URL("https://ideas-forum.herokuapp.com/avatar");
                try {
                    if (oauth2User.getAttribute("avatar") != null)
                        avatarUrl = new URL("https://cdn.discordapp.com/avatars/" + id + "/" + oauth2User.getAttribute("avatar") + ".png");
                    else if (oauth2User.getAttribute("avatar_url") != null)
                        avatarUrl = new URL("" + oauth2User.getAttribute("avatar_url"));
                } catch (MalformedURLException ignored) { }

                String username = oauth2User.getAttribute("global_name") != null ? oauth2User.getAttribute("global_name") :
                        oauth2User.getAttribute("username") != null ? oauth2User.getAttribute("username") :
                                oauth2User.getAttribute("login") != null ? oauth2User.getAttribute("login") : "Username";

                String email = oauth2User.getAttribute("email");

                String provider = oauth2User.getAttribute("global_name") != null ? "discord" :
                        oauth2User.getAttribute("username") != null ? "discord" : "github";

                // attributes to signing up
                session.setAttribute("oauth2Id", id);
                session.setAttribute("oauth2Username", username);
                session.setAttribute("oauth2Email", email);
                session.setAttribute("oauth2AvatarUrl", avatarUrl);
                session.setAttribute("oauth2Provider", provider);

                //  to clear able previous autofill data
                session.setAttribute("authData", null);
                return "redirect:/sign-up?oauth2=true";
            }

        } else return "redirect:/login";
    }
}
