package com.krokochik.ideasforum.config;

import com.krokochik.ideasforum.model.functional.UserAuth;
import com.krokochik.ideasforum.service.jdbc.UserService;
import com.krokochik.ideasforum.service.security.SecurityRoutineProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;

@Configuration
public class ThymeleafBeanConfig {

    @Autowired
    UserService userService;

    @Autowired
    SecurityRoutineProvider srp;

    @Bean(name = "userAuth")
    public UserAuth userAuth() {
        return new UserAuth() {
            @Override
            public String getCurrentEmail() {
                return userService.findByUsernameOrUnknown(srp
                                .getContext().getAuthentication().getName())
                        .getEmail();
            }

            @Override
            public String getCurrentName() {
                return srp.getContext().getAuthentication().getName();
            }

            @Override
            public Long getCurrentId() {
                return userService.findByUsernameOrUnknown(srp
                                .getContext().getAuthentication().getName())
                        .getId();
            }

            public String getNickname() {
                if (srp.isAuthenticated()) {
                    return userService.findByUsernameOrUnknown(srp
                                    .getContext().getAuthentication().getName())
                            .getNickname();
                } else return "guest";
            }

            public String getAvatar() {
                return new String(userService.findByUsernameOrUnknown(srp
                                .getContext().getAuthentication().getName())
                        .getAvatar(), StandardCharsets.UTF_8);
            }

            public boolean isMfaActivated() {
                return userService.findByUsernameOrUnknown(srp
                                .getContext().getAuthentication().getName())
                        .isMfaActivated();
            }

            public boolean isAuth() {
                return srp.isAuthenticated();
            }
        };
    }
}
