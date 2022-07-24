package com.krokochik.CampfireGallery.repository;

import java.util.HashMap;

public class VariableRepository {
    private final HashMap<String, String> stringValues = new HashMap<>();
    
    public void addValue(String name, String value) throws Exception {
        stringValues.put(name, value);
    }
    
    public String getValue(String name) throws NullPointerException {
        stringValues.get(name);
        return stringValues.get(name);
    }

    public void changeValue(String name, String newValue) throws NullPointerException {
        stringValues.get(name);
        stringValues.put(name, newValue);
    }

    public void removeValue(String name) throws NullPointerException {
        stringValues.remove(name);
    }
}
