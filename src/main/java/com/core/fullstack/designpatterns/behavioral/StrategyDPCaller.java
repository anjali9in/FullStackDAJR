package com.core.fullstack.designpatterns.behavioral;

import java.util.List;

public class StrategyDPCaller {

    public static void main(String[] args) {
        CheckoutService checkoutService = new CheckoutService(List.of(
                new CreditCardStrategy(),
                new PayPalStrategy()));
        CheckoutRequest checkoutRequest = new CheckoutRequest("PAYPAL");
        checkoutService.checkout(checkoutRequest);
    }
    
}
