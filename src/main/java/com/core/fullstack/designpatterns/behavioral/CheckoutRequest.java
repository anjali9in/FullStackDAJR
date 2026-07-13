package com.core.fullstack.designpatterns.behavioral;

public class CheckoutRequest {

    private final String type;

    public CheckoutRequest(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}