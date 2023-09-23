package com.krokochik.ideasforum.controller.auth;

import com.krokochik.ideasforum.model.db.User;
import com.krokochik.ideasforum.service.jdbc.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("mfa")
public class MfaCollector {

    @Autowired
    UserService userService;

    @GetMapping
    public String mfaPage() {
        User user = userService.findByUsernameOrUnknown(
                SecurityContextHolder.getContext().getAuthentication().getName()
        );
        if (user.equals(User.unknown())) {

        }
        return null;
    }
}
