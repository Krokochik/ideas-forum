package com.krokochik.ideasForum.config;

import com.krokochik.ideasForum.controller.SecurityController;
import com.krokochik.ideasForum.model.UserAuth;
import com.krokochik.ideasForum.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;

@Configuration
public class BeanConfig {
    @Autowired
    UserRepository userRepository;

    @Bean(name = "userAuth")
    public UserAuth userAuth() {
        return new UserAuth() {
            @Override
            public String getCurrentEmail() {
                return userRepository.findByUsername(SecurityController.getContext().getAuthentication().getName()).getEmail();
            }

            @Override
            public String getCurrentName() {
                return SecurityController.getContext().getAuthentication().getName();
            }

            @Override
            public Long getCurrentId() {
                return userRepository.findByUsername(SecurityController.getContext().getAuthentication().getName()).getId();
            }

            public String getNickname() {
                if (SecurityController.isAuthenticated()) {
                    return userRepository.findByUsername(SecurityController.getContext().getAuthentication().getName()).getNickname();
                }
                else return "guest";
            }

            public String getAvatar() {
                return new String(userRepository.findByUsername(SecurityController.getContext().getAuthentication().getName()).getAvatar(), StandardCharsets.UTF_8);
            }

            public boolean isMfaConnected() {
                return userRepository.findByUsername(SecurityController.getContext().getAuthentication().getName()).isMfaConnected();
            }

            public boolean isAuth() {
                return SecurityController.isAuthenticated();
            }
        };
    }
}
