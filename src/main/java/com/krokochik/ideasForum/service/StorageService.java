package com.krokochik.ideasForum.service;

import javax.websocket.Session;
import java.util.HashMap;
import java.util.Map;

public class StorageService<K, V> {

    HashMap<Session, HashMap<K, V>> sessionStorage;
    HashMap<K, V> storage;

    public StorageService() {
        sessionStorage = new HashMap<>();
        storage = new HashMap<>();
    }

    public void save(Session session, K key, V value) {
        sessionStorage.put(session, new HashMap<>() {{
            put(key, value);
        }});
    }

    public void save(Session session, Map<K, V> map) {
        map.keySet().forEach(key -> save(session, key, map.get(key)));
    }

    public void save(K key, V value) {
        storage.put(key, value);
    }

    public void remove(Session session) {
        sessionStorage.remove(session);
    }

    public void remove(Session session, K key) {
        sessionStorage.get(session).remove(key);
    }

    public void remove(K key) {
        storage.remove(key);
    }


    public void save(Map<K, V> map) {
        map.keySet().forEach(key -> save(key, map.get(key)));
    }

    public Map<K, V> get(Session session) {
        return sessionStorage.get(session);
    }

    public V get(Session session, K key) {
        return sessionStorage.get(session).get(key);
    }

    public V get(K key) {
        return storage.get(key);
    }

}
