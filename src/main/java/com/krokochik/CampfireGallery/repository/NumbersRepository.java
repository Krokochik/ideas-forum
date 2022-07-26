package com.krokochik.CampfireGallery.repository;

import java.security.SecureRandom;

public class NumbersRepository {

    public Integer generateNumber(int min, int max) {
        return new SecureRandom().nextInt(min, max+1);
    }
}
