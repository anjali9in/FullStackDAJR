package com.core.fullstack.designpatterns;

public class Hatchback implements Car {

    public Hatchback() {
        super();
        System.out.println("Hatchback car created.");
    }

    @Override
    public void assemble() {
        System.out.println("Assembling Hatchback car.");
    }

    @Override
    public void engine() {
        System.out.println("Installing engine in Hatchback car.");
    }
}