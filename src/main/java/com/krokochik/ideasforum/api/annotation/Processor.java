package com.krokochik.ideasforum.api.annotation;

import com.krokochik.ideasforum.api.processor.RequestProcessor;

import java.lang.annotation.*;

/**
 * Creates the object with type specified int the value and
 * assigns its value to the last parameter with corresponding
 * type. Injects values to created class' fields from parameters
 * with matching names. Also processes {@link FromParent} annotation.
 **/

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Processor {
    Class<? extends RequestProcessor> value();
}
