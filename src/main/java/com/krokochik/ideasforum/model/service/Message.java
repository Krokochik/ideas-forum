package com.krokochik.ideasforum.model.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    private HashMap<String, String> body;

    public Object get(String key) {
        return body.get(key);
    }

    public Message put(String key, String value) {
        body.put(key, value);
        return this;
    }

    public Object remove(String key) {
        return body.remove(key);
    }
}
