package com.krokochik.ideasforum.api.validator;

import java.util.concurrent.atomic.AtomicBoolean;

public interface RequestValidator <T> {
    T validate(AtomicBoolean a);
}
