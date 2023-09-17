package com.krokochik.ideasforum.api.annotation;

import com.krokochik.ideasforum.api.validator.RequestValidator;

import java.lang.annotation.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Creates the object with type specified int the value. Injects
 * values to created class' fields from parameters with matching names.
 * Calls {@link RequestValidator#validate(AtomicBoolean)}. If it returns
 * false, prevents execution of annotated method. Also processes
 * {@link FromParent} annotation.
 **/

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Validator {
    Class<? extends RequestValidator> value();
}
