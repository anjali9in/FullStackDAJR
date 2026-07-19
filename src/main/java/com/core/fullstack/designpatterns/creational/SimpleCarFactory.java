package com.core.fullstack.designpatterns.creational;

import com.core.fullstack.designpatterns.Car;
import com.core.fullstack.designpatterns.Hatchback;
import com.core.fullstack.designpatterns.Sedan;

public class SimpleCarFactory {
    public Car createCar(String type) {
        if (type.equalsIgnoreCase("sedan")) {
            return new Sedan();
        } else if (type.equalsIgnoreCase("hatchback")) {
            return new Hatchback();
        } else {
            throw new IllegalArgumentException("Unknown car type: " + type);
        }
    }
}


