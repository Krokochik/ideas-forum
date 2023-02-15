package com.krokochik.ideasForum.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

@Data
@ToString
@RequiredArgsConstructor
public class Message {

    @NotNull
    HashMap<String, String> content;

    public Message() {
        content = new HashMap<>();
    }

    public Message(Object key, Object value) {
        content = new HashMap<>() {{
            put(key.toString(), value.toString());
        }};
    }

    public void put(Object key, Object value) {
        content.put(key.toString(), value.toString());
    }

    public void remove(Object key) {
        content.remove(key.toString());
    }

    public String get(Object key) {
        return content.get(key.toString());
    }

}

