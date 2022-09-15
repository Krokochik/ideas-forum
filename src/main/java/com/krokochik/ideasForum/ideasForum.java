package com.krokochik.ideasForum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.InetAddress;

@SpringBootApplication
public class ideasForum {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(ideasForum.class, args);
		InetAddress inetAddress;
		inetAddress = InetAddress.getLocalHost();
		System.out.println(inetAddress.getHostName());
	}
}
