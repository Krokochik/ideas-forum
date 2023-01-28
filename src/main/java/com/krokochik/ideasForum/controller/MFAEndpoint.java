package com.krokochik.ideasForum.controller;

import com.krokochik.ideasForum.config.CustomSpringConfigurator;
import com.krokochik.ideasForum.model.Message;
import com.krokochik.ideasForum.service.MessageDecoder;
import com.krokochik.ideasForum.service.MessageEncoder;
import com.krokochik.ideasForum.service.StorageService;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(
        value = "/mfa",
        decoders = MessageDecoder.class,
        encoders = MessageEncoder.class,
        configurator = CustomSpringConfigurator.class)
@Component
public class MFAEndpoint {

    StorageService<String, String> storage = new StorageService<>();

    @OnOpen
    public void onOpen(Session session) {
    }

    @OnMessage
    public void onMessage(Session session, Message message) {
        if (!message.getContent().get("msg").equals("ping"))
            System.out.println(message);

    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.out.println("MFA ERROR");
        throwable.printStackTrace();
    }

}
