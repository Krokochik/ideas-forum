package com.krokochik.ideasForum.controller;

import com.krokochik.ideasForum.config.CustomSpringConfigurator;
import com.krokochik.ideasForum.model.CallbackTask;
import com.krokochik.ideasForum.model.Condition;
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
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

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
    StorageService<Session, String> sessionKeyStorage = new StorageService<>();
    StorageService<Session, MessageCipher> messageCipherStorage = new StorageService<>();
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
            if ((message.getContent().size() == 1) && message.getContent().containsKey("username")) {
                new Thread(() -> authenticate(session, message.get("username")));
            }
        }
    }

    private void authenticate(Session session, String login) {
        System.out.println("auth");
        SRP6ServerSession serverSession = new SRP6ServerSession(params);
        BigInteger salt = new BigInteger(userRepo.findByUsername(login).getSalt());
        BigInteger B = serverSession.step1(login, salt,
                new BigInteger(userRepo.findByUsername(login).getVerifier()));

        session.getAsyncRemote().sendObject(new Message() {{
            put("B", B);
            put("s", salt);
        }});
        System.out.println("sent 1");
        try {
            Message response = waitForMessage((message) -> {
                return (message.getContent().containsKey("A") && message.getContent().containsKey("M1"));
            }, 10_000L, session);

            BigInteger M2 = serverSession.step2(
                    new BigInteger(response.get("A")),
                    new BigInteger(response.get("M1")));

            String sessionKey = serverSession.getSessionKey(false).toString(16);
            sessionKeyStorage.save(session, sessionKey);

            MessageCipher cipher = new MessageCipher("username", "keyId", "ivId");
            messageCipherStorage.save(session, cipher);

            int keyId = (int) Math.floor(Math.random() * AESKeys.keys.length);
            int ivId = (int) Math.floor(Math.random() * AESKeys.keys.length);

            response = new Message() {{
                put("authenticated", "true");
                put("ivId", ivId);
                put("keyId", keyId);
            }};
            session.getAsyncRemote().sendObject(cipher.encrypt(response,
                    TokenService.getHash(login + sessionKey, AESKeys.keys[ivId]),
                    TokenService.getHash(login + sessionKey, AESKeys.keys[keyId])));
        } catch (TimeoutException | SRP6Exception e) {

        }
    }

    private void setOnMessage(CallbackTask<Message> task, Session session) {
        onMessageTasksStorage.get(session, "onMessage").add(task);
    }

    public void removeOnMessage(CallbackTask<Message> task, Session session) {
        onMessageTasksStorage.get(session, "onMessage").remove(task);
    }

    public Message waitForMessage(final Condition<Message> test, final Long timeout, Session session) throws TimeoutException {
        AtomicReference<Message> msg = new AtomicReference<>();
        AtomicBoolean wait = new AtomicBoolean(true);

        AtomicReference<CallbackTask<Message>> onMessageTask = new AtomicReference<>();
        onMessageTask.set((message) -> {
            for (int i = 0; i < message.getContent().size(); i++) {
                if (test.check(message)) {
                    msg.set(message);
                    wait.set(false);
                    removeOnMessage(onMessageTask.get(), session);
                }
            }
        });

        new Timer().schedule(new TimerTask() {
            public void run() {
                msg.set(null);
                wait.set(false);
            }
        }, timeout);

        setOnMessage(onMessageTask.get(), session);

        while (wait.get()) {
        }

        if (msg.get() == null)
            throw new TimeoutException();
        return msg.get();
    }

    @SneakyThrows
    public Message waitForMessage(final Condition<Message> test, Session session) {
        return waitForMessage(test, Long.MAX_VALUE, session);
    }

    @SneakyThrows
    public Message waitForMessage(final Long timeout, Session session) {
        return waitForMessage((msg) -> true, timeout, session);
    }

    @SneakyThrows
    public Message waitForMessage(Session session) {
        return waitForMessage((msg) -> true, Long.MAX_VALUE, session);
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.out.println("MFA ERROR");
        throwable.printStackTrace();
    }

}
