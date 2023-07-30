package com.krokochik.ideasForum.model.service;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Mail
{
    String theme;
    String link;
    String receiver;
}
