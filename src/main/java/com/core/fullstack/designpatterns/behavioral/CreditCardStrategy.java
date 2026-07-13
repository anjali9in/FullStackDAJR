package com.core.fullstack.designpatterns.behavioral;

import org.springframework.stereotype.Component;

@Component
public class CreditCardStrategy implements PaymentStrategy {

    @Override
    public void pay() {
        System.out.println("Processing payment using credit card");
    }

    @Override
    public String getType() {
        return "CREDIT_CARD";
    }
}