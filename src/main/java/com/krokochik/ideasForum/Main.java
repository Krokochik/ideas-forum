package com.krokochik.ideasForum;

import com.krokochik.ideasForum.model.Message;
import com.krokochik.ideasForum.service.MessageCipher;
import com.krokochik.ideasForum.service.TokenService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class Main {

    private static final String[] HOSTS = {"ideas-forum.herokuapp.com", "localhost:6606"};
    public static final String HOST = HOSTS[1];

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Main.class, args);

        final short IV_LENGTH = 203;
        final short SECRET_KEY_LENGTH = 5460;

        MessageCipher cipher = new MessageCipher(
                new TokenService().generateToken((long) SECRET_KEY_LENGTH),
                new TokenService().generateToken((long) IV_LENGTH), "username", "trash");

        Message message = new Message();
        message.put("username", "Nick");
        message.put("trash", "2q35hjiu5h87dgi3u[[g141");
        message.put("A", "166758345");

        Message encryptedMessage = cipher.encrypt(message);
        System.out.println(encryptedMessage.getContent());
        System.out.println(cipher.decrypt(encryptedMessage).getContent());

    }


}
