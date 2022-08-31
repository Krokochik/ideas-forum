package com.krokochik.ideasForum.model;

import lombok.ToString;
import lombok.Data;

@Data
@ToString
public class Mail {

    private String theme;
    private String link;

    private String receiver;

}
