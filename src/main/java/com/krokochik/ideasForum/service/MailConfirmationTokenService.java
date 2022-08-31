package com.krokochik.ideasForum.service;

import com.krokochik.ideasForum.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

@Service
public class MailConfirmationTokenService {
    @Autowired
    UserRepository userRepository;

    private static final Byte STANDARD_TOKEN_LENGTH = 40;
    private final Byte tokenLength;

    public MailConfirmationTokenService(Byte tokenLength) {
        this.tokenLength = tokenLength;
    }

    public MailConfirmationTokenService() {
        tokenLength = STANDARD_TOKEN_LENGTH;
    }

    public String generateToken(Byte tokenLength) {
        String chars = shuffle("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");
        SecureRandom secureRandom = new SecureRandom();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < tokenLength; i++) {
            builder.append(chars.charAt(secureRandom.nextInt(62)));
        }
        return builder.toString();
    }

    public String generateToken() {
        return generateToken(tokenLength);
    }

    private String shuffle(String string) {
        List<Character> characters = new ArrayList<>();
        StringBuilder builder = new StringBuilder(string.length());

        for(char chr : string.toCharArray()){
            characters.add(chr);
        }

        while(characters.size() != 0){
            int randPicker = (int) (Math.random() * characters.size());
            builder.append(characters.remove(randPicker));
        }

        return builder.toString();
    }
}
