package com.krokochik.ideasForum.service.crypto;

import at.favre.lib.crypto.SingleStepKdf;
import lombok.SneakyThrows;
import org.apache.commons.codec.binary.Base64;
import org.jetbrains.annotations.NotNull;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
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

}
