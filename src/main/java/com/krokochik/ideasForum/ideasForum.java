package com.krokochik.ideasForum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class }, scanBasePackages={"com.krokochik.ideasForum.controller",
"com.krokochik.ideasForum.config", "com.krokochik.ideasForum.entity", "com.krokochik.ideasForum", "com.krokochik.ideasForum.repository", "com.krokochik.ideasForum.rest",
"com.krokochik.ideasForum.service"})
public class ideasForum {
	public static void main(String[] args) throws Exception { SpringApplication.run(ideasForum.class, args); }
}
