package com.krokochik.ideasForum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ideasForum {

	private static final String[] HOSTS = {"ideas-forum.herokuapp.com", "localhost:6606"};
	public static final String HOST = HOSTS[1];

	public static void main(String[] args) throws Exception {
		SpringApplication.run(ideasForum.class, args);
	}
}
