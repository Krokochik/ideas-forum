package com.krokochik.CampfireGallery.repository;

import org.jetbrains.annotations.NotNull;
import org.springframework.lang.NonNull;

import java.util.HashMap;

public class VariableRepository {
    private final HashMap<String, String> stringValues = new HashMap<>();
    
    public void addValue(@NotNull String name, @NotNull String value) throws Exception {
        stringValues.put(name, value);
    }
    
    public String getValue(@NotNull String name) throws NullPointerException {
        stringValues.get(name);
        return stringValues.get(name);
    }

    public void changeValue(@NotNull String name, @NotNull String newValue) throws NullPointerException {
        stringValues.get(name);
        stringValues.put(name, newValue);
    }

    public void removeValue(@NotNull String name) throws NullPointerException {
        stringValues.remove(name);
    }
}
