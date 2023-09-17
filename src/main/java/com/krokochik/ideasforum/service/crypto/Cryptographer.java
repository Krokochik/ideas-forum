package com.krokochik.ideasforum.service.crypto;

import at.favre.lib.crypto.SingleStepKdf;
import dev.samstevens.totp.code.HashingAlgorithm;
import lombok.NonNull;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Functional class containing static methods that are working with cryptography.
 * */
public class Cryptographer {

    @Autowired
    private static HashingAlgorithm HASHING_ALGORITHM;
    private static final String AES_ALGORITHM = "AES/CBC/PKCS5PADDING";

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
                                 @NonNull String initVector) {
        if (str.isEmpty()) throw new IllegalArgumentException("This parameter can't be empty: str");
        if (secretKey.trim().isEmpty()) throw new IllegalArgumentException("This parameter can't be blank: secretKey");

        try {
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
        } catch (Exception unreachable) {
            return "";
        }
    }

    /**
     * Decrypts a string with AES algorithm.
     *
     * @param str        any nonempty string to be encrypted.
     * @param secretKey  any-length non-blank string to be used by AES as a key.
     * @param initVector any-length string to be used by AES as a parameter.
     * @return decrypted string.
     * @throws IllegalBlockSizeException inherited from {@link javax.crypto.Cipher}.
     */
    public static String decrypt(@NonNull String str,
                                 @NonNull String secretKey,
                                 @NonNull String initVector) throws IllegalBlockSizeException {
        try {
            IvParameterSpec iv = new IvParameterSpec(
                    SingleStepKdf.fromSha256()
                            .derive(initVector.getBytes(StandardCharsets.UTF_8), 16));
            SecretKeySpec key = new SecretKeySpec(
                    SingleStepKdf.fromSha256().derive(secretKey
                            .getBytes(StandardCharsets.UTF_8), 16), "AES");
            byte[] subject = str.getBytes();

            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key, iv);

            return new String(cipher.doFinal(Base64.decodeBase64(subject)));
        } catch (IllegalBlockSizeException e) {
            throw new IllegalBlockSizeException(e.getMessage());
        } catch (Exception unreachable) {
            return "";
        }
    }

    /**
     * Computes the MD5 hash of the given object and returns a string representation of the hash.
     *
     * @param o the object to compute the hash for
     * @return a string representation of the MD5 hash
     * @throws RuntimeException if the MD5 algorithm is not available
     */
    public static String getHash(Object o) {
        try {
            MessageDigest md = MessageDigest.getInstance("md5");
            byte[] bytes;
            if (o instanceof byte[]) {
                bytes = (byte[]) o;
            } else {
                bytes = o.toString().getBytes();
            }
            bytes = md.digest(bytes);
            String base64Hash = Base64.encodeBase64String(bytes);
            return base64Hash.substring(0, 10);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
