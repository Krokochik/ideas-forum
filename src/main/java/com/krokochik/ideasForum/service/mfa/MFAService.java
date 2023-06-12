package com.krokochik.ideasForum.service.mfa;

import com.krokochik.ideasForum.model.Token;
import com.krokochik.ideasForum.service.crypto.TokenService;
import com.nimbusds.srp6.SRP6CryptoParams;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class MFAService {

    @Autowired
    TokenService tokenService;

    private final static SRP6CryptoParams PARAMS = SRP6CryptoParams.getInstance(2048, "SHA-512");
    private final HashMap<String, Token> tokens = new HashMap<>();

    private static final long TOKEN_PART_LENGTH = 9;

    /**
     * @return Returns token, consisted of endpoint and crypto key.
     */
    public Token addNewConnectionToken(@NotNull @NonNull String username) {
        Token token = new Token(tokenService.generateToken(TOKEN_PART_LENGTH),
                tokenService.generateToken(TOKEN_PART_LENGTH));
        tokens.put(username, (token));
        return token;
    }

    public Optional<String> getUsernameByPublicTokenPart(String publicPart) {
        AtomicReference<String> result = new AtomicReference<>("");
        tokens.forEach((username, token) -> {
            if (publicPart.equals(token.getPublicPart()))
                result.set(username);
        });

        if (result.get().equals(""))
            result.set(null);

        return Optional.ofNullable(result.get());
    }

    public Optional<Token> getToken(@NonNull String username) {
        return Optional.ofNullable(tokens.get(username));
    }

    public void removeToken(@NonNull String username) {
        tokens.remove(username);
    }

}
