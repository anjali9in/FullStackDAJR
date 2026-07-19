package com.core.fullstack.designpatterns.creational;

import com.core.fullstack.designpatterns.Car;

public class SimpleFactoryClient {

    public static void main(String[] args) {
        SimpleCarFactory factory = new SimpleCarFactory();

        Car sedan = factory.createCar("sedan");
        sedan.assemble();
        sedan.engine();

        Car hatchback = factory.createCar("hatchback");
        hatchback.assemble();
        hatchback.engine();
    }
}

class MahindraCars {
    public static void main(String[] args) {
        SimpleCarFactory factory = new SimpleCarFactory();

        Car sedan = factory.createCar("sedan");
        sedan.assemble();
        sedan.engine();
    }

}