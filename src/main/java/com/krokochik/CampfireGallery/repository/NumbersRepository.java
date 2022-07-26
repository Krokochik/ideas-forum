package com.krokochik.CampfireGallery.repository;

import java.util.Random;

public class NumbersRepository {

    public Integer generateNumber(int min, int max) {
        return new Random().nextInt(min, max);
    }
}
