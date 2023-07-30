package com.krokochik.ideasForum.config;

import com.krokochik.ideasForum.model.functional.UserAuth;
import com.krokochik.ideasForum.repository.UserRepository;
import com.krokochik.ideasForum.service.security.SecurityRoutineProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;

@Configuration
public class ThymeleafBeanConfig {

    @Autowired
    UserRepository userRepository;

    @Autowired
    SecurityRoutineProvider srp;

    @Bean(name = "userAuth")
    public UserAuth userAuth() {
        return new UserAuth() {
            @Override
            public String getCurrentEmail() {
                return userRepository.findByUsername(srp.getContext().getAuthentication().getName()).getEmail();
            }

            @Override
            public String getCurrentName() {
                return srp.getContext().getAuthentication().getName();
            }

            @Override
            public Long getCurrentId() {
                return userRepository.findByUsername(srp.getContext().getAuthentication().getName()).getId();
            }

            public String getNickname() {
                if (srp.isAuthenticated()) {
                    return userRepository.findByUsername(srp.getContext().getAuthentication().getName()).getNickname();
                }
                else return "guest";
            }

            public String getAvatar() {
                return new String(userRepository.findByUsername(srp.getContext().getAuthentication().getName()).getAvatar(), StandardCharsets.UTF_8);
            }

            public boolean isMfaConnected() {
                return userRepository.findByUsername(srp.getContext().getAuthentication().getName()).isMfaConnected();
            }

            public boolean isAuth() {
                return srp.isAuthenticated();
            }
        };
    }
}
