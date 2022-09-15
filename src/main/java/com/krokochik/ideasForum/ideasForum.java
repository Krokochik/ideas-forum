package com.krokochik.ideasForum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ideasForum {

	public static final String HOST = "ideas-forum.herokuapp.com";

	public static void main(String[] args) throws Exception {
		SpringApplication.run(ideasForum.class, args);
	}
}
