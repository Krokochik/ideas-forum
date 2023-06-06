package com.krokochik.ideasForum.controller;

import com.krokochik.ideasForum.config.CustomSpringConfigurator;
import com.krokochik.ideasForum.model.CallbackTask;
import com.krokochik.ideasForum.model.Message;
import com.krokochik.ideasForum.repository.UserRepository;
import com.krokochik.ideasForum.res.AESKeys;
import com.krokochik.ideasForum.service.crypto.MessageCipher;
import com.krokochik.ideasForum.service.crypto.TokenService;
import com.krokochik.ideasForum.service.mfa.MessageDecoder;
import com.krokochik.ideasForum.service.mfa.MessageEncoder;
import com.krokochik.ideasForum.service.mfa.Storage;
import com.nimbusds.srp6.*;
import lombok.NonNull;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

@ServerEndpoint(
        value = "/mfa",
        decoders = MessageDecoder.class,
        encoders = MessageEncoder.class,
        configurator = CustomSpringConfigurator.class)
@Component
public class MFAEndpoint {

    @Autowired
    UserRepository userRepo;

    Storage<String, ArrayList<CallbackTask<Message>>> onMessageTasksStorage = new Storage<>();
    HashMap<String, String> sessionKeys = new HashMap<>();
    HashMap<Session, SRP6ServerSession> serverSessions = new HashMap<>();
    HashMap<Session, BigInteger> B = new HashMap<>();
    HashMap<Session, BigInteger> salts = new HashMap<>();
    HashMap<Session, String> logins = new HashMap<>();
    MessageCipher cipher = new MessageCipher("username", "keyId", "ivId", "content");
    SRP6CryptoParams params = SRP6CryptoParams.getInstance(2048, "SHA-512");

    @OnOpen
    public void onOpen(Session session) {
        onMessageTasksStorage.save(session, "onMessage", new ArrayList<>());
    }

    @OnMessage
    public void onMessage(Session session, Message message) {
        if ((message.getContent().get("msg") == null) || !message.getContent().get("msg").equals("ping")) {
            System.out.println(message);

            new Thread(() -> onMessageTasksStorage.get(session, "onMessage").forEach(task -> {
                try {
                    task.run(message);
                } catch (Exception ignored) {
                }
            })).start();

            if ((message.getContent().size() == 1) && message.getContent().containsKey("username"))
                authenticateStepOne(message.get("username"), session);
            if (message.getContent().containsKey("A") && message.getContent().containsKey("M1") && message.getContent().containsKey("username"))
                authenticateStepTwo(message.get("A"), message.get("M1"), session);

            if ((message.getContent().size() > 1) && message.getContent().containsKey("username"))
                processMessage(message, session);
        }

    }

    private void processMessage(Message message, Session session) {
        if (message.getContent().containsKey("get"))
            getRequestProcessor(message, session);
    }

    private void getRequestProcessor(Message message, Session session) {
        Message response;
        try {
            switch (message.get("get")) {
                case "avatar" -> {
                    response = new Message("avatar", new String(
                            userRepo.findByUsername(message.get("username"))
                                    .getAvatar()));
                    session.getAsyncRemote().sendObject(response);
                    System.out.println("avatar sent");
                    return;
                }
                case "email" -> response = new Message("email",
                        userRepo.findByUsername(message.get("username"))
                                .getEmail());
                case "authCheckingMessage" -> {
                    response = new Message(new HashMap<>() {{
                        put("content", "authCheckingMessage");
                        put("test", "test");
                    }});
                }
                default -> response = new Message("error", "unknown request");
            }
            response = encrypt(response, message.get("username"));
        } catch (NullPointerException nullPointerException) {
            response = new Message("error", "unknown username");
        }
        session.getAsyncRemote().sendObject(response);
    }

    private Message encrypt(@NonNull Message message, String username) {
        int keyId = (int) Math.floor(Math.random() * AESKeys.keys.length);
        int ivId = (int) Math.floor(Math.random() * AESKeys.keys.length);

        message.put("keyId", keyId);
        message.put("ivId", keyId);
        val sessionKey = sessionKeys.get(username);
        System.out.println(sessionKey);
        return cipher.encrypt(message,
                TokenService.getHash(AESKeys.keys[ivId], username + sessionKey),
                TokenService.getHash(AESKeys.keys[keyId], username + sessionKey));
    }

    private Optional<Message> decrypt(@NonNull Message message) {
        if (!message.getContent().containsKey("keyId") || !message.getContent().containsKey("ivId"))
            return Optional.empty();

        val username = message.get("username");
        val sessionKey = sessionKeys.get(username);
        return Optional.of(cipher.decrypt(message,
                TokenService.getHash(AESKeys.keys[Integer.parseInt(message.get("ivId"))], username + sessionKey),
                TokenService.getHash(AESKeys.keys[Integer.parseInt(message.get("keyId"))], username + sessionKey)));
    }

    private void authenticateStepOne(String login, Session session) {
        new Thread(() -> {
            SRP6ServerSession serverSession = new SRP6ServerSession(params);
            BigInteger salt = new BigInteger(userRepo.findByUsername(login).getSalt());
            BigInteger B = serverSession.step1(login, salt,
                    new BigInteger(userRepo.findByUsername(login).getVerifier()));
            serverSessions.put(session, serverSession);
            this.B.put(session, B);
            salts.put(session, salt);
            logins.put(session, login);

            session.getAsyncRemote().sendObject(new Message(new HashMap<>() {{
                put("B", B.toString());
                put("s", salt.toString());
            }}));
        }).start();
    }

    private void authenticateStepTwo(String A, String M1, Session session) {
        SRP6ServerSession serverSession = serverSessions.get(session);
        String login = logins.get(session);
        BigInteger salt = salts.get(session);
        BigInteger B = this.B.get(session);

        if ((login != null) && (serverSession != null) && (salt != null) && (B != null)) {
            try {
                SRP6ClientSession clientSession = new SRP6ClientSession();
                clientSession.step1(login, userRepo.findByUsername(login).getPassword());
                SRP6ClientCredentials credentials = clientSession.step2(params, salts.get(session), this.B.get(session));

                serverSession.step2(new BigInteger(A), new BigInteger(M1));

                String sessionKey = serverSession.getSessionKey().toString(16);
                sessionKeys.put(login, sessionKey);

                int keyId = (int) Math.floor(Math.random() * AESKeys.keys.length);
                int ivId = (int) Math.floor(Math.random() * AESKeys.keys.length);


                Message response = new Message(new HashMap<>() {{
                    put("test", "test");
                    put("ivId", ivId + "");
                    put("keyId", keyId + "");
                }});
                session.getAsyncRemote().sendObject(cipher.encrypt(response,
                        TokenService.getHash(AESKeys.keys[ivId], login + sessionKey),
                        TokenService.getHash(AESKeys.keys[keyId], login + sessionKey)));
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
