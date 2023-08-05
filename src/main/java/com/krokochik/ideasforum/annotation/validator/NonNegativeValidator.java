package com.krokochik.ideasforum.annotation.validator;

import com.krokochik.ideasforum.annotation.NonNegative;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NonNegativeValidator implements ConstraintValidator<NonNegative, Number> {
    @Override
    public boolean isValid(Number number, ConstraintValidatorContext constraintValidatorContext) {
        if (number == null) {
            throw new NullPointerException("Value cannot be null");
        }
        if (number.doubleValue() < 0) {
            throw new IllegalArgumentException("Value must be non-negative");
        }

        return true;
    }

    @Override
    public void initialize(NonNegative constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }
}
