package com.krokochik.ideasforum.model.functional;

@FunctionalInterface
public interface CallbackTask<V> {
    void run(V value);
}
