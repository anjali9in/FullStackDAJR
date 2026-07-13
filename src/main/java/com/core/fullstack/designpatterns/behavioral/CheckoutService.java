package com.core.fullstack.designpatterns.behavioral;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;

@Service
public class CheckoutService {

    private final Map<String, PaymentStrategy> paymentStrategies;

    public CheckoutService(List<PaymentStrategy> paymentStrategies) {
        this.paymentStrategies = new HashMap<>();
        for (PaymentStrategy paymentStrategy : paymentStrategies) {
            this.paymentStrategies.put(Objects.requireNonNull(paymentStrategy.getType()), paymentStrategy);
        }
    }

    public void checkout(CheckoutRequest request) {
        if (!paymentStrategies.containsKey(request.getType())) {
            throw new IllegalArgumentException("Unsupported payment type: " + request.getType());
        }
        paymentStrategies.get(request.getType()).pay();
    }
}