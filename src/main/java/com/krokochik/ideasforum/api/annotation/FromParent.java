package com.krokochik.ideasforum.api.annotation;

import java.lang.annotation.*;

/**
 * Marks field to value injection from parent if possible.
 * (for objects created by {@link Validator} or {@link Processor})
 **/

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FromParent {
}
