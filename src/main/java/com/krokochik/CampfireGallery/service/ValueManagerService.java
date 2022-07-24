package com.krokochik.CampfireGallery.service;

import com.krokochik.CampfireGallery.repository.VariableRepository;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ValueManagerService {
    private ArrayList<VariableRepository> repositories = new ArrayList<>();

    public Integer addRepository() {
        repositories.add(new VariableRepository());
        return repositories.size() - 1;
    }

    public String getVariable(String name, int repoId) throws IndexOutOfBoundsException, NullPointerException {
        return repositories.get(repoId).getValue(name);
    }

    public void addVariable(String name, String value, int repoId) throws Exception {
        repositories.get(repoId).addValue(name, value);
    }

    public void changeVariable(String name, String newValue, int repoId) throws NullPointerException {
        repositories.get(repoId).changeValue(name, newValue);
    }

    public boolean isRepositoryExist(int repoId) {
        try { repositories.get(repoId); } catch (IndexOutOfBoundsException | NullPointerException e) { return false; }
        return true;
    }

    public ValueManagerService(@NotNull ArrayList<VariableRepository> repositories) {
        this.repositories = repositories;
    }

    public ValueManagerService(){
        repositories.add(new VariableRepository());
    }

}
