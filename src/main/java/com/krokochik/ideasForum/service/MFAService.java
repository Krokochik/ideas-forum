package com.krokochik.ideasForum.service;

import com.krokochik.ideasForum.model.User;
import com.nimbusds.srp6.SRP6CryptoParams;
import com.nimbusds.srp6.SRP6VerifierGenerator;

import java.math.BigInteger;

public class MFAService {

    protected final byte generator = 2;
    protected final static byte radix = 16;
    static final SRP6CryptoParams config = SRP6CryptoParams.getInstance(1536, "SHA-512");

    public static User writeSaltAndVerifier(User user) {
        SRP6VerifierGenerator verifierGenerator = new SRP6VerifierGenerator(config);

        BigInteger salt = new BigInteger(SRP6VerifierGenerator.generateRandomSalt(radix));
        BigInteger verifier = verifierGenerator.generateVerifier(salt, user.getPassword());

        user.setSalt(salt.toString(radix));
        user.setVerifier(verifier.toString(radix));

        return user;
    }

}
