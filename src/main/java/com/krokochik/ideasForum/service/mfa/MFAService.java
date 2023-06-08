package com.krokochik.ideasForum.service.mfa;

import com.krokochik.ideasForum.model.User;
import com.krokochik.ideasForum.service.crypto.TokenService;
import com.nimbusds.srp6.SRP6CryptoParams;
import com.nimbusds.srp6.SRP6VerifierGenerator;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Optional;

@Service
public class MFAService {

    @Autowired
    TokenService tokenService;

    private final static SRP6CryptoParams PARAMS = SRP6CryptoParams.getInstance(2048, "SHA-512");
    private final HashMap<String, String> tokens = new HashMap<>();

    /**
     * @return Returns token, consisted of endpoint and crypto key.
     */
    public String addNewConnectionToken(@NotNull @NonNull String username) {
        String token =  tokenService.generateToken(18L);
        tokens.put(username, token);
        return token;
    }

    public Optional<String> getToken(@NonNull String username) {
        return Optional.ofNullable(tokens.get(username));
    }

    public void removeToken(@NonNull String username) {
        tokens.remove(username);
    }

    public static User writeSaltAndVerifier(@NonNull User user) {
        SRP6VerifierGenerator verifierGenerator = new SRP6VerifierGenerator(PARAMS);
        BigInteger salt;
        do {
            salt = new BigInteger(verifierGenerator.generateRandomSalt());
        } while (salt.signum() != 1);
        BigInteger verifier = verifierGenerator.generateVerifier(salt, user.getPassword());

        user.setSalt(salt.toString());
        user.setVerifier(verifier.toString());

        return user;
    }

}
