package com.krokochik.ideasForum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class ideasForum {

    private static final String[] HOSTS = {"ideas-forum.herokuapp.com", "localhost:6606"};
    public static final String HOST = HOSTS[0];

    public static void main(String[] args) {
        SpringApplication.run(ideasForum.class, args);
    }
}
