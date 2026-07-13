package com.core.fullstack.designpatterns.behavioral;

import org.springframework.stereotype.Component;

@Component
public class PayPalStrategy implements PaymentStrategy {

    @Override
    public void pay() {
        System.out.println("Processing payment using PayPal");
    }

    @Override
    public String getType() {
        return "PAYPAL";
    }
}