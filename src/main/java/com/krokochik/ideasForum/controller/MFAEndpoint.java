package com.krokochik.ideasForum.controller;

import com.krokochik.ideasForum.config.CustomSpringConfigurator;
import com.krokochik.ideasForum.model.Message;
import com.krokochik.ideasForum.service.MessageDecoder;
import com.krokochik.ideasForum.service.MessageEncoder;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.HashMap;

@ServerEndpoint(
        value = "/mfa",
        decoders = MessageDecoder.class,
        encoders = MessageEncoder.class,
        configurator = CustomSpringConfigurator.class)
@Component
public class MFAEndpoint {

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("OPENED");
        session.getAsyncRemote().sendObject(new Message(new HashMap<>(){{put("msg", "hello client!");}}));
    }

    @OnMessage
    public void onMessage(Session session, Message message) {
        if(!message.getContent().get("msg").equals("ping")) {
            System.out.println("MESSAGE");
            System.out.println(message);
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        System.out.println("CLOSED");
        System.out.println(closeReason.getCloseCode());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.out.println("ERROR");
        throwable.printStackTrace();
    }

}
