package com.krokochik.CampfireGallery.model;

public class Number {
    int id;
    int value;

    protected void setId(int id) {
        this.id = id;
    }

    protected void setValue(int value) {
        this.value = value;
    }

    public Number(int value, int id) {
        this.id = id;
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public int getValue() {
        return value;
    }

    public Number(){}
}
