package com.krokochik.ideasforum.service.mfa;

import com.krokochik.ideasforum.model.service.Token;
import com.krokochik.ideasforum.service.crypto.TokenService;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class MFAService {

    @Autowired
    TokenService tokenService;

    private final HashMap<String, Token> tokens = new HashMap<>();

    private static final long TOKEN_PART_LENGTH = 9;

    /**
     * Generates and stores in temp memory an mfa connection token,
     * consisted of endpoint and crypto key, and associated with a specific user.
     *
     * @return the token.
     * @throws NullPointerException if the parameter is {@code null}.
     */
    public Token addNewConnectionToken(@NonNull String username) {
        Token token = new Token(tokenService.generateToken(TOKEN_PART_LENGTH),
                tokenService.generateToken(TOKEN_PART_LENGTH));
        tokens.put(username, (token));
        return token;
    }

    /**
     * Searches the username, the token associated with, by the token's public part.
     *
     * @param publicPart the public token's part.
     * @return the username.
     * @throws NullPointerException if public part is {@code null}.
     **/
    public Optional<String> getUsernameByPublicTokenPart(@NonNull String publicPart) {
        AtomicReference<String> result = new AtomicReference<>("");
        tokens.forEach((username, token) -> {
            if (token.getPublicPart().equals(publicPart))
                result.set(username);
        });

        if ("".equals(result.get()))
            return Optional.empty();

        return Optional.of(result.get());
    }

    /**
     * Obtains the token associated with the username
     *
     * @throws NullPointerException if the parameter is {@code null}.
     **/
    public Optional<Token> getToken(@NonNull String username) {
        Token token = tokens.get(username);
        if (token == null)
            return Optional.empty();
        return Optional.of(token);
    }

    /**
     * Deletes from the storage the token associated with the username
     *
     * @throws NullPointerException if the parameter is {@code null}.
     **/
    public void removeToken(@NonNull String username) {
        tokens.remove(username);
    }

}
