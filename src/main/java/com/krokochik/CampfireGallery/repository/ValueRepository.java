package com.krokochik.CampfireGallery.repository;

import java.util.HashMap;

public class ValueRepository {
    private final HashMap<String, String> stringValues = new HashMap<>();
    
    public boolean addValue(String name, String value) {
        try { getValue(name); } catch (NullPointerException nullPointerException) { return false; }
        stringValues.put(name, value);
        return true;
    }
    
    public String getValue(String name) {
        try { stringValues.get(name); } catch (NullPointerException nullPointerException) { return null; }
        return stringValues.get(name);
    }
    
    public void removeValue(String name) {
        try {
            stringValues.remove(name);
        } catch (NullPointerException ignored){}
    }
}
