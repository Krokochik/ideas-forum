package com.krokochik.CampfireGallery.repository;

public class NumbersRepository {

    public Integer generateNumber() {
        return (int) (Math.random() * 6) + 1;
    }
}
