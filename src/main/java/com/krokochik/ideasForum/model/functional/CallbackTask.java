package com.krokochik.ideasForum.model.functional;

@FunctionalInterface
public interface CallbackTask<V> {
    void run(V value);
}
