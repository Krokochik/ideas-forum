package com.krokochik.ideasForum.service.crypto;

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

    public MessageCipher(@NotNull String... skippedKeys) {
        this.skipKeys = new ArrayList<>();

        skipKeys.addAll(Arrays.stream(skippedKeys)
                .map(String::toLowerCase)
                .collect(Collectors.toList()));
    }


    @SneakyThrows
    public String encrypt(String str, String initVector, String secretKey) {
        IvParameterSpec ivParameter = new IvParameterSpec(SingleStepKdf.fromSha256().derive(initVector.getBytes(StandardCharsets.UTF_8), 16));
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        SecretKeySpec keySpec = new SecretKeySpec(SingleStepKdf.fromSha256().derive(secretKey.getBytes(StandardCharsets.UTF_8), 16), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivParameter);

        return Base64.encodeBase64String(cipher.doFinal(str.getBytes()));
    }

    @SneakyThrows
    public String decrypt(String str, String initVector, String secretKey) {
        IvParameterSpec ivParameter = new IvParameterSpec(SingleStepKdf.fromSha256().derive(initVector.getBytes(StandardCharsets.UTF_8), 16));
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        SecretKeySpec keySpec = new SecretKeySpec(SingleStepKdf.fromSha256().derive(secretKey.getBytes(StandardCharsets.UTF_8), 16), "AES");
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivParameter);

        return new String(cipher.doFinal(Base64.decodeBase64(str)));
    }

    @SneakyThrows
    public Message encrypt(Message message, String initVector, String secretKey) {
        Message result = new Message(new HashMap<>());

        for (String key : message.getContent().keySet()) {
            if (!skipKeys.contains(key.toLowerCase())) {
                result.put(
                        encrypt(key, initVector, secretKey),
                        encrypt(message.get(key), initVector, secretKey)
                );
            } else result.put(key, message.get(key));
        }

        return result;
    }

    @SneakyThrows
    public Message decrypt(Message message, String initVector, String secretKey) {
        Message result = new Message(new HashMap<>());

        for (String key : message.getContent().keySet()) {
            if (!skipKeys.contains(key.toLowerCase())) {
                result.put(
                        decrypt(key, initVector, secretKey),
                        decrypt(message.get(key), initVector, secretKey)
                );
            } else result.put(key, message.get(key));
        }

        return result;
    }
}
