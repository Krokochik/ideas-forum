package com.krokochik.ideasforum.annotation;

import com.krokochik.ideasforum.annotation.validator.NonNegativeValidator;
import jakarta.validation.Constraint;

import java.lang.annotation.*;

/**
 * Will check if a value is non-null and non-negative.
 **/
@Documented
@Constraint(validatedBy = NonNegativeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD, ElementType.LOCAL_VARIABLE})
@Retention(RetentionPolicy.RUNTIME)
public @interface NonNegative {
}
