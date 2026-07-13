package com.core.fullstack.designpatterns.creational;

public class SingletonCaller {
    

    public static void main(String[] args) {
        Singleton singleton1 = Singleton.getInstance();
        Singleton singleton2 = Singleton.getInstance();

        System.out.println("Singleton 1: " + singleton1);
        System.out.println("Singleton 2: " + singleton2);

        SingletonEnum singletonEnum1 = SingletonEnum.INSTANCE;
        SingletonEnum singletonEnum2 = SingletonEnum.INSTANCE;

        SingletonClassHolder singletonClassHolder1 = SingletonClassHolder.getInstance();
        SingletonClassHolder singletonClassHolder2 = SingletonClassHolder.getInstance();

        // Check if both instances are the same
        System.out.println("Are both instances the same? " + (singleton1 == singleton2));
        System.out.println("Are both enum instances the same? " + (singletonEnum1 == singletonEnum2));
        System.out.println("Are both class holder instances the same? " + (singletonClassHolder1 == singletonClassHolder2));
    }
}
