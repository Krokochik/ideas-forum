package com.krokochik.ideasForum.service;

import com.google.gson.Gson;
import com.krokochik.ideasForum.model.Message;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

public class MessageEncoder implements Encoder.Text<Message> {

    private static Gson gson = new Gson();

    @Override
    public String encode(Message msg) throws EncodeException {
        return gson.toJson(msg);
    }

    @Override
    public void init(EndpointConfig endpointConfig) {}

    @Override
    public void destroy() {}
}

