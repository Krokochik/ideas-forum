package com.krokochik.ideasForum.controller;

import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

@ServerEndpoint(value = "/mfa/auth")
public class SRPAuthenticationEndpoint {

    Session session;

    @OnOpen
    public void onOpen(Session session) throws IOException {

    }

}
