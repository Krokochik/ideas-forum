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
import com.krokochik.ideasForum.service.mfa.StorageService;
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

    StorageService<String, ArrayList<CallbackTask<Message>>> onMessageTasksStorage = new StorageService<>();
    HashMap<String, String> sessionKeys = new HashMap<>();
    HashMap<Session, SRP6ServerSession> serverSessions = new HashMap<>();
    HashMap<Session, BigInteger> B = new HashMap<>();
    HashMap<Session, BigInteger> salts = new HashMap<>();
    HashMap<Session, String> logins = new HashMap<>();
    MessageCipher cipher = new MessageCipher("username", "keyId", "ivId");
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
        message = decrypt(message).orElseGet(Message::new);
        if (message.getContent().containsKey("get"))
            getRequestProcessor(message, session);
    }

    private void getRequestProcessor(Message message, Session session) {
        Message response;
        switch (message.get("get")) {
            case "avatar" -> response = new Message("avatar",
                    userRepo.findByUsername(message.get("username"))
                    .getAvatar());
            case "email" -> response = new Message("email",
                    userRepo.findByUsername(message.get("username"))
                    .getEmail());
            case "auth" -> {
                if (sessionKeys.get(message.get("username")) != null)
                    response = new Message("auth", "true");
                else
                    response = new Message("auth", "false");

                session.getAsyncRemote().sendObject(response);
                return;
            }
            default -> response = new Message();
        }
        response = encrypt(response, message.get("username"));
        session.getAsyncRemote().sendObject(response);
    }

    private Message encrypt(@NonNull Message message, String username) {
        int keyId = (int) Math.floor(Math.random() * AESKeys.keys.length);
        int ivId = (int) Math.floor(Math.random() * AESKeys.keys.length);

        message.put("keyId", keyId);
        message.put("ivId", keyId);
        val sessionKey = sessionKeys.get(username);
        return cipher.encrypt(message,
                TokenService.getHash(username + sessionKey, AESKeys.keys[ivId]),
                TokenService.getHash(username + sessionKey, AESKeys.keys[keyId]));
    }

    private Optional<Message> decrypt(@NonNull Message message) {
        if (!message.getContent().containsKey("keyId") || !message.getContent().containsKey("ivId"))
            return Optional.empty();

        val username = message.get("username");
        val sessionKey = sessionKeys.get(username);
        return Optional.of(cipher.decrypt(message,
                TokenService.getHash(username + sessionKey, AESKeys.keys[Integer.parseInt(message.get("ivId"))]),
                TokenService.getHash(username + sessionKey, AESKeys.keys[Integer.parseInt(message.get("keyId"))])));
    }

    private void authenticateStepOne(String login, Session session) {
        new Thread(() -> {
            System.out.println("auth");
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
            System.out.println("sent 1");
        }).start();
    }

    private void authenticateStepTwo(String A, String M1, Session session) {
        System.out.println("auth 2");
        SRP6ServerSession serverSession = serverSessions.get(session);
        String login = logins.get(session);
        BigInteger salt = salts.get(session);
        BigInteger B = this.B.get(session);

        if ((login != null) && (serverSession != null) && (salt != null) && (B != null)) {
            System.out.println("step 2");
            try {
                SRP6ClientSession clientSession = new SRP6ClientSession();
                clientSession.step1(login, userRepo.findByUsername(login).getPassword());
                SRP6ClientCredentials credentials = clientSession.step2(params, salts.get(session), this.B.get(session));
                System.out.println(credentials.A);
                System.out.println(credentials.M1);

                serverSession.step2(new BigInteger(A), new BigInteger(M1));

                String sessionKey = serverSession.getSessionKey().toString(16);
                System.out.println(sessionKey);
                sessionKeys.put(login, sessionKey);

                int keyId = (int) Math.floor(Math.random() * AESKeys.keys.length);
                int ivId = (int) Math.floor(Math.random() * AESKeys.keys.length);


                Message response = new Message(new HashMap<>() {{
                    put("authenticated", "true");
                    put("ivId", ivId + "");
                    put("keyId", keyId + "");
                }});
                session.getAsyncRemote().sendObject(cipher.encrypt(response,
                        TokenService.getHash(login + sessionKey, AESKeys.keys[ivId]),
                        TokenService.getHash(login + sessionKey, AESKeys.keys[keyId])));
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
