package com.krokochik.ideasforum.annotation.validator;

import com.krokochik.ideasforum.annotation.NonBlank;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NonBlankValidator implements ConstraintValidator<NonBlank, String> {
    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        if (s == null) {
            throw new NullPointerException("Value cannot be null");
        }
        if (s.isBlank()) {
            throw new IllegalArgumentException("Value cannot be blank");
        }

        return true;
    }

    @Override
    public void initialize(NonBlank constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }
}
