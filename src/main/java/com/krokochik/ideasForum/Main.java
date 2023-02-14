package com.krokochik.ideasForum;

import com.krokochik.ideasForum.res.AESKeys;
import com.krokochik.ideasForum.service.TokenService;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class Main {

    private static final String[] HOSTS = {"ideas-forum.herokuapp.com", "localhost:6606"};
    public static final String HOST = HOSTS[0];

    public static void main(String[] args) throws Exception {
        //SpringApplication.run(Main.class, args);

        System.out.println(TokenService.getHash(
                TokenService.getHash(AESKeys.tokens[(int) Math.floor(Math.random() * AESKeys.tokens.length)], "login"), "pass"));

    }


}
