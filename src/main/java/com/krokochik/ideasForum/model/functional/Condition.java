package com.krokochik.ideasForum.model.functional;

@FunctionalInterface
public interface Condition<V> {
    Boolean check(V value);
}
