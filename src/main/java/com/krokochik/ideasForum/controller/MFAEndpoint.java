package com.krokochik.ideasForum.controller;

import com.krokochik.ideasForum.config.CustomSpringConfigurator;
import com.krokochik.ideasForum.model.Message;
import com.krokochik.ideasForum.service.MessageDecoder;
import com.krokochik.ideasForum.service.MessageEncoder;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;

@ServerEndpoint(
        value = "/mfa",
        decoders = MessageDecoder.class,
        encoders = MessageEncoder.class,
        configurator = CustomSpringConfigurator.class)
@Component
public class MFAEndpoint {

    @OnOpen
    public void onOpen(Session session) throws IOException {
        System.out.println("OPENED");
        session.getAsyncRemote().sendObject(new Message(new HashMap<>(){{put("msg", "hello client!");}}));
    }

    @OnMessage
    public void onMessage(Session session, Message message) throws IOException {
        System.out.println("MESSAGE");
        System.out.println(message);
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) throws IOException {
        System.out.println("CLOSED");
        System.out.println(closeReason.getCloseCode());
        System.out.println(closeReason.getReasonPhrase());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.out.println("ERROR");
        throwable.printStackTrace();
    }

}
