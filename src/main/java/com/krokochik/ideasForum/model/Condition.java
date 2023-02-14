package com.krokochik.ideasForum.model;

@FunctionalInterface
public interface Condition<V> {
    Boolean check(V value);
}
