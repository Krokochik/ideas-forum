package com.krokochik.ideasForum.config;

import com.krokochik.ideasForum.controller.MFAEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebSocketConfiguration {

    @Bean
    public MFAEndpoint mfaEndpoint() {
        return new MFAEndpoint();
    }
}
