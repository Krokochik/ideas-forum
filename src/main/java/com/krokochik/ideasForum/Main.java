package com.krokochik.ideasForum;

import com.krokochik.ideasForum.model.User;
import com.krokochik.ideasForum.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;


@SpringBootApplication
public class Main {

    private static final String[] HOSTS = {"ideas-forum.herokuapp.com", "localhost:6606"};
    public static final String HOST = HOSTS[0];

    @Autowired
    static UserRepository userRepository;

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);

        User[] users = userRepository.getAllUsers();
        Arrays.stream(users).forEach(User::startMfaCodeGenerating);
    }

}
