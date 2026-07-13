package com.core.fullstack.designpatterns.creational;

import java.io.Serializable;

public class SingletonClassHolder implements Serializable {
    
    private SingletonClassHolder() {
        // Private constructor to prevent instantiation
    }

    private static class Holder {
        private static final SingletonClassHolder INSTANCE = new SingletonClassHolder();
    }

    public static SingletonClassHolder getInstance() {
        return Holder.INSTANCE;
    }

    private Object readResolve() {
        return getInstance();
    }
}