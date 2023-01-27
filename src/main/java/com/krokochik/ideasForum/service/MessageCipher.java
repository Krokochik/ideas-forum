package com.krokochik.ideasForum.service;

import at.favre.lib.crypto.SingleStepKdf;
import com.krokochik.ideasForum.model.Message;
import lombok.SneakyThrows;
import org.apache.commons.codec.binary.Base64;
import org.jetbrains.annotations.NotNull;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

public class MessageCipher {

    ArrayList<String> skipKeys;
    String secretKey, initVector;

    public MessageCipher(String secretKey, String initVector, @NotNull String... skippedKeys) {
        this.skipKeys = new ArrayList<>();
        this.secretKey = secretKey;
        this.initVector = initVector;

        skipKeys.addAll(Arrays.stream(skippedKeys)
                .map(String::toLowerCase)
                .collect(Collectors.toList()));
    }

    @SneakyThrows
    public Message encrypt(Message message) {

        IvParameterSpec ivParameter = new IvParameterSpec(SingleStepKdf.fromSha256().derive(initVector.getBytes(StandardCharsets.UTF_8), 16));
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        SecretKeySpec keySpec = new SecretKeySpec(SingleStepKdf.fromSha256().derive(secretKey.getBytes(StandardCharsets.UTF_8), 16), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivParameter);

        Message result = new Message(new HashMap<>());

        for (String key : message.getContent().keySet()) {
            if (!skipKeys.contains(key.toLowerCase())) {
                result.put(
                        Base64.encodeBase64String(cipher.doFinal(key.getBytes())),
                        Base64.encodeBase64String(cipher.doFinal(message.getContent().get(key).getBytes()))
                );
            } else result.put(key, message.get(key));
        }

        return result;
    }

    @SneakyThrows
    public Message decrypt(Message message) {

        IvParameterSpec ivParameter = new IvParameterSpec(SingleStepKdf.fromSha256().derive(initVector.getBytes(StandardCharsets.UTF_8), 16));
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        SecretKeySpec keySpec = new SecretKeySpec(SingleStepKdf.fromSha256().derive(secretKey.getBytes(StandardCharsets.UTF_8), 16), "AES");
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivParameter);

        Message result = new Message(new HashMap<>());

        for (String key : message.getContent().keySet()) {
            if (!skipKeys.contains(key.toLowerCase())) {
                result.put(
                        new String(cipher.doFinal(Base64.decodeBase64(key))),
                        new String(cipher.doFinal(Base64.decodeBase64(message.getContent().get(key))))
                );
            } else result.put(key, message.get(key));
        }

        return result;

    }

}
