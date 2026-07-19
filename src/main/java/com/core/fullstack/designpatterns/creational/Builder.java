package com.core.fullstack.designpatterns.creational;

import lombok.ToString;

public record Builder(String orderId, String customerName, String deliveryAddress, boolean expressDelivery) {

	private Builder(CustomOrderBuilder builder) {
		this(builder.orderId, builder.customerName, builder.deliveryAddress, builder.expressDelivery);
	}

	@Override
	public String toString() {
		return "Builder{" +
				"orderId='" + orderId + '\'' +
				", customerName='" + customerName + '\'' +
				", deliveryAddress='" + deliveryAddress + '\'' +
				", expressDelivery=" + expressDelivery + '\'' +
				'}';
	}

    public static class CustomOrderBuilder {
        private final String orderId;
        private final String customerName;
        private String deliveryAddress = "Not Provided";
        private boolean expressDelivery;

        public CustomOrderBuilder(String orderId, String customerName) {
            this.orderId = orderId;
            this.customerName = customerName;
        }

        public CustomOrderBuilder deliveryAddress(String deliveryAddress) {
            this.deliveryAddress = deliveryAddress;
            return this;
        }

        public CustomOrderBuilder expressDelivery(boolean expressDelivery) {
            this.expressDelivery = expressDelivery;
            return this;
        }

        public Builder build() {
            if (orderId == null || orderId.isBlank()) {
                throw new IllegalArgumentException("orderId is required");
            }
            if (customerName == null || customerName.isBlank()) {
                throw new IllegalArgumentException("customerName is required");
            }
            return new Builder(this);
        }
    }
    
    public static void main(String[] args) {
        Builder order = new Builder.CustomOrderBuilder("ORD-1001", "Anjali")
                .deliveryAddress("Bengaluru, India")
                .expressDelivery(true)
                .build();

        System.out.println(order);
    }
}
