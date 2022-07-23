package com.krokochik.CampfireGallery.repository;

import com.krokochik.CampfireGallery.model.Number;
import java.util.ArrayList;

public class NumbersRepository extends Number {
    public NumbersRepository(){
        super();
    }
    private ArrayList<Integer> numbers = new ArrayList<>();

    public Number generateNumber() {
        int num = (int) (Math.random() * 5) + 1;
        numbers.add(num);
        return new Number(num, numbers.size() - 1);
    }

    public Number getNumber(int id) throws IndexOutOfBoundsException {
        return new Number(numbers.get(id), id);
    }
}
