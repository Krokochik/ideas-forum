package com.krokochik.ideasforum.model.functional;

@FunctionalInterface
public interface Condition<V> {
    Boolean check(V value);
}
