package com.krokochik.CampfireGallery.repository;

import java.util.HashMap;

public class VariableRepository {
    private final HashMap<String, String> stringValues = new HashMap<>();
    
    public void addValue(String name, String value) throws Exception {
        if(name == null || value == null)
            throw new NullPointerException();
        stringValues.put(name, value);
    }
    
    public String getValue(String name) throws NullPointerException {
        if(stringValues.get(name) == null)
            throw new NullPointerException();
        return stringValues.get(name);
    }

    public void changeValue(String name, String newValue) throws NullPointerException {
        if(stringValues.get(name) == null)
            throw new NullPointerException();
        stringValues.put(name, newValue);
    }

    public void removeValue(String name) throws NullPointerException {
        if(stringValues.get(name) == null)
            throw new NullPointerException();
        stringValues.remove(name);
    }
}
