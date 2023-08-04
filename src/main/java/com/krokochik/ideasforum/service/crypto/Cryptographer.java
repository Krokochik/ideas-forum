package com.krokochik.ideasforum.service.crypto;

import at.favre.lib.crypto.SingleStepKdf;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Functional class containing static methods that are working with cryptography.
 * */
public class Cryptographer {

    private static final String AES_ALGORITHM = "AES/CBC/PKCS5PADDING";
    private static final String HASH_ALGORITHM = "SHA-512";

    /**
     * Encrypts a string with AES algorithm.
     *
     * @param str        any nonempty string to be encrypted.
     * @param secretKey  any-length non-blank string to be used by AES as a key.
     * @param initVector any-length string to be used by AES as a parameter.
     * @return encrypted string.
     */
    public static String encrypt(@NonNull String str,
                                 @NonNull String secretKey,
                                 @NonNull String initVector) throws InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
        if (str.isEmpty()) throw new IllegalArgumentException("This parameter can't be empty: str");
        if (secretKey.isBlank()) throw new IllegalArgumentException("This parameter can't be blank: secretKey");

        IvParameterSpec iv = new IvParameterSpec(
                SingleStepKdf.fromSha256().derive(initVector
                        .getBytes(StandardCharsets.UTF_8), 16));
        SecretKeySpec key = new SecretKeySpec(
                SingleStepKdf.fromSha256().derive(secretKey
                        .getBytes(StandardCharsets.UTF_8), 16), "AES");
        byte[] subject = str.getBytes();

        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);

        return Base64.encodeBase64String(cipher.doFinal(subject));
    }

    /**
     * Decrypts a string with AES algorithm.
     *
     * @param str        any nonempty string to be encrypted.
     * @param secretKey  any-length non-blank string to be used by AES as a key.
     * @param initVector any-length string to be used by AES as a parameter.
     * @return decrypted string.
     * @throws BadPaddingException inherited from {@link jakarta.crypto.Cipher}.
     */
    public static String decrypt(@NonNull String str,
                                 @NonNull String secretKey,
                                 @NonNull String initVector) throws InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
        IvParameterSpec iv = new IvParameterSpec(
                SingleStepKdf.fromSha256()
                        .derive(initVector.getBytes(StandardCharsets.UTF_8), 16));
        SecretKeySpec key = new SecretKeySpec(
                SingleStepKdf.fromSha256().derive(secretKey
                        .getBytes(StandardCharsets.UTF_8), 16), "AES");
        byte[] subject = str.getBytes();

        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key, iv);

        return new String(cipher.doFinal(subject));
    }

    /**
     * Computes SHA-512 hash of the string.
     */
    @SneakyThrows
    public static String getHash(String str, String salt) {
        str += salt;
        MessageDigest crypt = MessageDigest.getInstance(HASH_ALGORITHM);
        crypt.update(str.getBytes(StandardCharsets.UTF_8));

        byte[] bytes = crypt.digest();
        BigInteger bi = new BigInteger(1, bytes);
        return String.format("%0" + (bytes.length << 1) + "x", bi);
    }

    /**
     * Computes SHA-512 hash of the string.
     */
    public static String getHash(String str) {
        return getHash(str, "");
    }
}
