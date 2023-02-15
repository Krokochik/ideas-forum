package com.krokochik.ideasForum.service.mfa;

import com.krokochik.ideasForum.model.User;
import com.nimbusds.srp6.SRP6CryptoParams;
import com.nimbusds.srp6.SRP6VerifierGenerator;

import java.math.BigInteger;

public class MFAService {

    final static SRP6CryptoParams params = SRP6CryptoParams.getInstance(2048, "SHA-512");

    public static User writeSaltAndVerifier(User user) {
        SRP6VerifierGenerator verifierGenerator = new SRP6VerifierGenerator(params);

        BigInteger salt = new BigInteger(verifierGenerator.generateRandomSalt(16));
        BigInteger verifier = verifierGenerator.generateVerifier(salt, user.getPassword());

        user.setSalt(salt.toString());
        user.setVerifier(verifier.toString());

        return user;
    }

}
