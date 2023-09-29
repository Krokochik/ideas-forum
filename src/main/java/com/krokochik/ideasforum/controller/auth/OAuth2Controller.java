package com.krokochik.ideasforum.controller.auth;

import com.krokochik.ideasforum.model.db.User;
import com.krokochik.ideasforum.service.jdbc.UserService;
import com.krokochik.ideasforum.service.security.SecurityRoutineProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

import static org.springframework.security.core.context.SecurityContextHolder.getContext;

@Slf4j
@Controller
public class OAuth2Controller {

    @Autowired
    UserService userService;

    @Autowired
    SecurityRoutineProvider srp;

    @SneakyThrows
    @GetMapping("/oauth2/{status}")
    public String oauth2finished(OAuth2AuthenticationToken authentication,
                                 HttpSession session, HttpServletRequest request,
                                 HttpServletResponse response,
                                 @PathVariable(name = "status") String status) {
        log.info("oauth2");
        OAuth2User oauth2User;
        try {
            oauth2User = authentication.getPrincipal();
        } catch (NullPointerException exception) {
            return "redirect:/login";
        }

        oauth2User.getAttributes().forEach((s, o) -> log.info(s + ": " + o));
        if (oauth2User.getAttribute("id") != null || oauth2User.getAttribute("sub") != null) {
            String id;
            try {
                if (oauth2User.getAttribute("id") != null) {
                    id = oauth2User.getAttribute("id");
                } else if (oauth2User.getAttribute("sub") != null) {
                    id = oauth2User.getAttribute("sub");
                } else return "redirect:/login";
            } catch (NullPointerException e) {
                return "redirect:/login";
            }  catch (ClassCastException e) {
                id = oauth2User.getAttribute("id") + "";
            }

            User user;
            Optional<User> userOptional = userService.findUserByOAuth2Id(id);
            if (userOptional.isPresent()) {
                user = userOptional.get();
                log.info(user.toString());
                srp.authorizeUser(user, true, getContext(), request, response);
                return "redirect:/email-validity-confirmation";
            } else {
                URL avatarUrl = new URL("https://ideasforum-3e3f402d99b3.herokuapp.com/avatar");
                try {
                    if (oauth2User.getAttribute("avatar") != null) {
                        avatarUrl = new URL("https://cdn.discordapp.com/avatars/" + id + "/" + oauth2User.getAttribute("avatar") + ".png");
                    } else if (oauth2User.getAttribute("avatar_url") != null) {
                        avatarUrl = new URL("" + oauth2User.getAttribute("avatar_url"));
                    } else if (oauth2User.getAttribute("picture") != null) {
                        avatarUrl = new URL("" + oauth2User.getAttribute("picture"));
                    }
                } catch (MalformedURLException ignored) { }

                String username;
                if (oauth2User.getAttribute("global_name") != null) {
                    username = oauth2User.getAttribute("global_name");
                } else if (oauth2User.getAttribute("username") != null) {
                    username = oauth2User.getAttribute("username");
                } else if (oauth2User.getAttribute("login") != null) {
                    username = oauth2User.getAttribute("login");
                } else if ((username = oauth2User.getAttribute("given_name")) != null) {
                    username = username.concat((
                            (String) oauth2User.getAttribute("at_hash"))
                            .substring(0, 4));
                } else username = "Username";

                String provider = oauth2User.getAttribute("global_name") != null ? "discord" :
                        oauth2User.getAttribute("username") != null ? "discord" :
                                oauth2User.getAttribute("family_name") != null ? "google" : "github";

                String email = oauth2User.getAttribute("email");
                boolean emailVerified = email != null;
                if (emailVerified) {
                    if (provider.equals("github"))
                        emailVerified = false;
                    else if (provider.equals("google") &&
                            !(Boolean.TRUE).equals(oauth2User.getAttribute("email_verified"))) {
                        log.info(oauth2User.getAttribute("email_verified"));
                        emailVerified = false;
                    }
                }

                log.info("sign-up");
                // attributes to signing up
                session.setAttribute("oauth2Id", id);
                session.setAttribute("oauth2Username", username);
                session.setAttribute("oauth2Email", email);
                session.setAttribute("oauth2EmailVerified", emailVerified);
                session.setAttribute("oauth2AvatarUrl", avatarUrl);
                session.setAttribute("oauth2Provider", provider);

                //  to clear able previous autofill data
                session.setAttribute("authData", null);
                return "redirect:/sign-up?oauth2=true";
            }

        } else return "redirect:/login";
    }
}
