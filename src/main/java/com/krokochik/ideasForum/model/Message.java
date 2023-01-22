package com.krokochik.ideasForum.model;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

@Data
public class Message {

    @NotNull
    HashMap<String, String> content;

}

