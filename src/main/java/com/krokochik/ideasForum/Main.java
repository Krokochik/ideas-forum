package com.krokochik.ideasForum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;


@SpringBootApplication
public class Main {

    private static final String[] HOSTS = {"ideas-forum.herokuapp.com", "localhost:6606"};
    public static final String HOST = HOSTS[0];

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);

        File directory = new File("./src/main/resources/dynamic/");
        if (directory.exists())
            deleteFiles(directory);
    }

    public static void deleteFiles(File directory) {
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteFiles(file);
                } else {
                    file.delete();
                }
            }
        }
    }
}
