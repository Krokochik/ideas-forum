package com.krokochik.ideasForum.controller;

import com.krokochik.ideasForum.config.CustomSpringConfigurator;
import com.krokochik.ideasForum.model.Message;
import com.krokochik.ideasForum.repository.UserRepository;
import com.krokochik.ideasForum.res.AESKeys;
import com.krokochik.ideasForum.service.crypto.MessageCipher;
import com.krokochik.ideasForum.service.crypto.TokenService;
import com.krokochik.ideasForum.service.mfa.MessageDecoder;
import com.krokochik.ideasForum.service.mfa.MessageEncoder;
import com.krokochik.ideasForum.service.mfa.StorageService;
import com.nimbusds.srp6.SRP6CryptoParams;
import com.nimbusds.srp6.SRP6Exception;
import com.nimbusds.srp6.SRP6ServerSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.math.BigInteger;
import java.util.HashMap;

@ServerEndpoint(
        value = "/mfa",
        decoders = MessageDecoder.class,
        encoders = MessageEncoder.class,
        configurator = CustomSpringConfigurator.class)
@Component
public class MFAEndpoint {

    @Autowired
    UserRepository userRepo;

    StorageService<String, Object> sessionStorage = new StorageService<>();
    SRP6CryptoParams params = SRP6CryptoParams.getInstance(2048, "SHA-512");
    MessageCipher cipher = new MessageCipher("username", "keyId", "ivId");

    @OnOpen
    public void onOpen(Session session) {
    }

    @OnMessage
    public void onMessage(Session session, Message message) {
        if ((message.getContent().get("msg") == null) || !message.getContent().get("msg").equals("ping")) {
            System.out.println(message);

            if ((message.getContent().size() == 1) && message.getContent().containsKey("username"))
                authenticateStepOne(message.get("username"), session);
            if (message.getContent().containsKey("A") && message.getContent().containsKey("M1"))
                authenticateStepTwo(message.get("A"), message.get("M1"), session);
        }

    }

    private void authenticateStepOne(String username, Session session) {
        new Thread(() -> {
            System.out.println("auth");
            SRP6ServerSession serverSession = new SRP6ServerSession(params);
            BigInteger salt = new BigInteger(userRepo.findByUsername(username).getSalt());
            BigInteger B = serverSession.step1(username, salt,
                    new BigInteger(userRepo.findByUsername(username).getVerifier()));

            sessionStorage.save(session, "serverSession", serverSession);
            sessionStorage.save(session, "username", username);

            session.getAsyncRemote().sendObject(new Message(new HashMap<>(){{
                put("B", B.toString());
                put("s", salt.toString());
            }}));
            System.out.println("sent 1");
        }).start();
    }

    private void authenticateStepTwo(String A, String M1, Session session) {
        System.out.println("auth 2");
        SRP6ServerSession serverSession = (SRP6ServerSession) sessionStorage.get(session, "serverSession");
        System.out.println(sessionStorage.get(session, "serverSession"));


        if (serverSession != null) {
            System.out.println("step 2");
            try {
                serverSession.step2(new BigInteger(A), new BigInteger(M1));

                String sessionKey = serverSession.getSessionKey().toString(16);
                System.out.println(sessionKey);
                sessionStorage.save(session, "sessionKey", sessionKey);

                int keyId = (int) Math.floor(Math.random() * AESKeys.keys.length);
                int ivId = (int) Math.floor(Math.random() * AESKeys.keys.length);


                Message response = new Message(new HashMap<>() {{
                    put("authenticated", "true");
                    put("ivId", ivId + "");
                    put("keyId", keyId + "");
                }});

                String username = (String) sessionStorage.get(session, "username");
                session.getAsyncRemote().sendObject(cipher.encrypt(response,
                        TokenService.getHash(username + sessionKey, AESKeys.keys[ivId]),
                        TokenService.getHash(username + sessionKey, AESKeys.keys[keyId])));
                System.out.println("sent 2");
            } catch (SRP6Exception e) {
                e.printStackTrace();
            }
        }
    }


    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        System.out.println("closed");
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.out.println("MFA ERROR");
        throwable.printStackTrace();
    }

}
