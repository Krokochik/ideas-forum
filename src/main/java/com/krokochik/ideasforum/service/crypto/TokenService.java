package com.krokochik.ideasforum.service.crypto;

import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * This service is for generating different random strings: tokens, codes, etc.
 */
@Service
public class TokenService {

    private static final Long STANDARD_TOKEN_LENGTH = 40L;
    private final Long tokenLength;

    /**
     * @param tokenLength it will be used as default generating string length.
     *                    If negative, will be up to 0. If absent, will be set to 40.
     * */
    public TokenService(Long tokenLength) {
        this.tokenLength = Math.max(tokenLength, 0);
    }

    public TokenService() {
        tokenLength = STANDARD_TOKEN_LENGTH;
    }

    /**
     * Generates random string consisting of specified characters.
     *
     * @param source it is the enumeration of characters of which will be
     *               built a new random string. It should be in "qwerty12345" format and mustn't be empty or null.
     * @param tokenLength the token length. If negative, will be up to 0.
     * @throws NullPointerException if a parameter is {@code null}.
     **/
    public String generateToken(@NonNull Long tokenLength, @NonNull String source) {
        if (source.isEmpty()) throw new IllegalArgumentException("Source string must not be empty.");
        tokenLength = Math.max(tokenLength, 0);

        String chars = shuffle(source);
        SecureRandom secureRandom = new SecureRandom();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < tokenLength; i++) {
            builder.append(chars.charAt(secureRandom.nextInt(62)));
        }
        return builder.toString();
    }

    /**
     * Generates random string consisting of low and up case letters and numbers.
     *
     * @param tokenLength the token length. If negative, will be up to 0.
     * @throws NullPointerException if tokenLength is {@code null}.
     **/
    public String generateToken(@NonNull Long tokenLength) {
        return generateToken(tokenLength, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789012345678901234567890123456789".repeat(20));
    }

    /**
     * Generates random string of <i>default</i> length (see at constructor), consisting of low and up case letters and numbers.
     **/
    public String generateToken() {
        return generateToken(tokenLength, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789012345678901234567890123456789".repeat(20));
    }

    /**
     * Generates 6 characters long random string, consisting of numbers.
     **/
    public String generateMfaCode() {
        return generateToken(6L, "1234567890".repeat(100));
    }

    /**
     * Generates 4 characters long random string, consisting of numbers.
     **/
    public String generateMfaPIN() {
        return generateToken(4L, "1234567890".repeat(100));
    }

    /**
     * Generates 9 characters long random string, consisting of numbers.
     **/
    public String generateMfaResetCode() {
        return generateToken(9L, "1234567890".repeat(100));
    }

    private String shuffle(String string) {
        List<Character> characters = new ArrayList<>();
        StringBuilder builder = new StringBuilder(string.length());

        for(char chr : string.toCharArray()){
            characters.add(chr);
        }

        while(!characters.isEmpty()){
            int randPicker = (int) (Math.random() * characters.size());
            builder.append(characters.remove(randPicker));
        }

        return builder.toString();
    }

}
