package com.core.fullstack.designpatterns.creational;

public class BuilderCaller {

    public static void main(String[] args) {
        Builder order = new Builder.CustomOrderBuilder("ORD-1001", "Anjali")
                .deliveryAddress("Bengaluru, India")
                .expressDelivery(true)
                .build();

        System.out.println(order);
    }
}