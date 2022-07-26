package com.krokochik.CampfireGallery.model;

public class Key {
    private String key;
    private boolean valid = true;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Key(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return "Key{" +
                "key='" + key + '\'' +
                '}';
    }
}
