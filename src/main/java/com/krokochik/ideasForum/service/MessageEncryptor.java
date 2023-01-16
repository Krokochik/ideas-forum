package com.krokochik.ideasForum.service;

import com.google.gson.Gson;
import com.krokochik.ideasForum.model.Message;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

public class MessageEncryptor implements Encoder.Text<Message> {

    protected static Gson gson = new Gson();

    @Override
    public String encode(Message object) throws EncodeException {
        return null;
    }

    @Override
    public void init(EndpointConfig endpointConfig) {

    }

    @Override
    public void destroy() {

    }
}
