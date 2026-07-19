package com.core.fullstack.designpatterns;

public class Sedan implements Car {

    public Sedan() {
        System.out.println("Sedan car created.");
    }

    @Override
    public void assemble() {
        System.out.println("Assembling Sedan car.");
    }
    @Override
    public void engine() {
        System.out.println("Installing engine in Sedan car.");
    }
}