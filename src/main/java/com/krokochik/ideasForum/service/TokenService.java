package com.krokochik.ideasForum.service;

import com.krokochik.ideasForum.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

@Service
public class TokenService {
    @Autowired
    UserRepository userRepository;

    private static final Long STANDARD_TOKEN_LENGTH = 40L;
    private final Long tokenLength;

    public TokenService(Long tokenLength) {
        this.tokenLength = tokenLength;
    }

    public TokenService() {
        tokenLength = STANDARD_TOKEN_LENGTH;
    }

    public String generateToken(Long tokenLength) {
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

    public static String getHash(String str, String salt) {
        MessageDigest crypt = null;
        try { crypt = MessageDigest.getInstance("SHA3-512"); } catch (NoSuchAlgorithmException unreachable) {}
        crypt.update(str.getBytes(StandardCharsets.UTF_8));

        byte[] bytes = crypt.digest();
        BigInteger bi = new BigInteger(1, bytes);
        return String.format("%0" + (bytes.length << 1) + "x", bi);
    }

    public static String getHash(String str) {
        return getHash(str, "");
    }
}
