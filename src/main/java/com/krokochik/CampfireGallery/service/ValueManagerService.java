package com.krokochik.CampfireGallery.service;

import com.krokochik.CampfireGallery.repository.ValueRepository;

import java.util.ArrayList;
import java.util.HashMap;

public class ValueManagerService {
    private ArrayList<ValueRepository> repositories = new ArrayList<>();

    public Integer addRepository() {
        repositories.add(new ValueRepository());
        return repositories.size() - 1;
    }

    public String getVariable(String name, int repoId) throws IndexOutOfBoundsException {
        return repositories.get(repoId).getValue(name);
    }

    public void addVariable(String name, String value, int repoId) {
        repositories.get(repoId).addValue(name, value);
    }

    public boolean isExist(int id) {
        System.out.println("isExs: " + id);
        try { repositories.get(id); } catch (IndexOutOfBoundsException indexOutOfBoundsException) { return false; }
        return true;
    }

    public ValueManagerService(ArrayList<ValueRepository> repositories) {
        this.repositories = repositories;
    }

    public ValueManagerService(){}

}
