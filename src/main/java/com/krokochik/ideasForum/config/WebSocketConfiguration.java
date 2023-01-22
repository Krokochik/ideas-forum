package com.krokochik.ideasForum.config;

import com.krokochik.ideasForum.controller.MFAEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@Configuration
public class WebSocketConfiguration  {

    @Bean
    public MFAEndpoint mfaEndpoint() {
        return new MFAEndpoint();
    }

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
