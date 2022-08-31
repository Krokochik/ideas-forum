package com.krokochik.ideasForum.config;

import com.krokochik.ideasForum.controller.AuthController;
import com.krokochik.ideasForum.model.UserAuth;
import com.krokochik.ideasForum.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {
    @Autowired
    UserRepository userRepository;

    @Bean(name = "userAuth")
    public UserAuth userAuth() {
        return new UserAuth() {
            @Override
            public String getCurrentEmail() {
                return userRepository.findByUsername(AuthController.getContext().getAuthentication().getName()).getEmail();
            }

            @Override
            public String getCurrentName() {
                return AuthController.getContext().getAuthentication().getName();
            }

            @Override
            public Long getCurrentId() {
                return userRepository.findByUsername(AuthController.getContext().getAuthentication().getName()).getId();
            }
        };
    }
}
