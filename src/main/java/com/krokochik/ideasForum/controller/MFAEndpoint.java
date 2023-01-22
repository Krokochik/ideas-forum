package com.krokochik.ideasForum.controller;

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
        encoders = MessageEncoder.class)
@Component
public class MFAEndpoint {

    Session session;

    @OnOpen
    public void onOpen(Session session) throws IOException {
        session.getAsyncRemote().sendObject(new Message(new HashMap<>(){{put("hello", "world!");}}));
    }

    @OnMessage
    public void onMessage(Session session, Message message) throws IOException {
        System.out.println(message);
    }

    @OnClose
    public void onClose(Session session) throws IOException {
        // WebSocket connection closes
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        // Do error handling here
    }

}
