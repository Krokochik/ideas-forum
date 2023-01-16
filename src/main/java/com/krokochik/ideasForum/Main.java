package com.krokochik.ideasForum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class Main {

    public static final String HOST = "localhost:6606";

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Main.class, args);
    }


}
