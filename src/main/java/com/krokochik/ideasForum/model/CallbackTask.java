package com.krokochik.ideasForum.model;

@FunctionalInterface
public interface CallbackTask<V> {
    void run(V value);
}
