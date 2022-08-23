package com.krokochik.ideasForum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan
public class ideasForum {
	public static void main(String[] args) throws Exception {
		SpringApplication.run(ideasForum.class, args);
	}
}
