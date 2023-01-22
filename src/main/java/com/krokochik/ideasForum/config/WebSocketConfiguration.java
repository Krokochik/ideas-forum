package com.krokochik.ideasForum.config;

import com.krokochik.ideasForum.controller.MFAEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
public class WebSocketConfiguration implements WebSocketConfigurer {

    @Bean
    public MFAEndpoint mfaEndpoint() {
        return new MFAEndpoint();
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    }
}
