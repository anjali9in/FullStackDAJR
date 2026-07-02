package com.core.fullstack.designpatterns.architechtural.saga.orchestrator;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.UUID;

public class Orchestrator {

	public static void main(String[] args) {
		SagaOrchestrator sagaOrchestrator = new SagaOrchestrator(
			List.of(
				new InventoryReservationStep(),
				new PaymentStep(),
				new ShippingStep()
			)
		);

		String successOrderId = "ORD-" + UUID.randomUUID();
		SagaResult successResult = sagaOrchestrator.execute(
			new OrderRequest(successOrderId, 2, 5499.0, false, false)
		);
		printResult(successResult);

		String failureOrderId = "ORD-" + UUID.randomUUID();
		SagaResult failedResult = sagaOrchestrator.execute(
			new OrderRequest(failureOrderId, 1, 2199.0, true, false)
		);
		printResult(failedResult);
	}

	private static void printResult(SagaResult result) {
		System.out.println("\n================================");
		System.out.println("Order ID: " + result.orderId());
		System.out.println("Status  : " + result.status());
		System.out.println("Message : " + result.message());
		System.out.println("Events  :");
		for (String event : result.events()) {
			System.out.println(" - " + event);
		}
		System.out.println("================================");
	}

	enum SagaStatus {
		COMPLETED,
		COMPENSATED
	}

	record OrderRequest(
		String orderId,
		int quantity,
		double amount,
		boolean failPayment,
		boolean failShipping
	) {
	}

	record SagaResult(
		String orderId,
		SagaStatus status,
		String message,
		List<String> events
	) {
	}

	static class SagaContext {

		private final OrderRequest request;
		private final List<String> events = new ArrayList<>();

		private boolean inventoryReserved;
		private boolean paymentCaptured;
		private boolean shipmentCreated;

		SagaContext(OrderRequest request) {
			this.request = request;
			addEvent("Saga started at " + Instant.now());
		}

		void addEvent(String message) {
			events.add(message);
		}

		OrderRequest request() {
			return request;
		}

		List<String> events() {
			return List.copyOf(events);
		}

		boolean inventoryReserved() {
			return inventoryReserved;
		}

		void inventoryReserved(boolean inventoryReserved) {
			this.inventoryReserved = inventoryReserved;
		}

		boolean paymentCaptured() {
			return paymentCaptured;
		}

		void paymentCaptured(boolean paymentCaptured) {
			this.paymentCaptured = paymentCaptured;
		}

		boolean shipmentCreated() {
			return shipmentCreated;
		}

		void shipmentCreated(boolean shipmentCreated) {
			this.shipmentCreated = shipmentCreated;
		}
	}

	interface SagaStep {

		String name();

		void execute(SagaContext context);

		void compensate(SagaContext context);
	}

	static class SagaOrchestrator {

		private final List<SagaStep> steps;

		SagaOrchestrator(List<SagaStep> steps) {
			this.steps = steps;
		}

		SagaResult execute(OrderRequest request) {
			SagaContext context = new SagaContext(request);
			Deque<SagaStep> completed = new ArrayDeque<>();

			try {
				for (SagaStep step : steps) {
					context.addEvent("Executing step: " + step.name());
					step.execute(context);
					completed.push(step);
				}

				context.addEvent("Saga completed successfully.");
				return new SagaResult(
					request.orderId(),
					SagaStatus.COMPLETED,
					"Order workflow completed",
					context.events()
				);
			} catch (RuntimeException exception) {
				context.addEvent("Failure in saga: " + exception.getMessage());

				// Compensate in reverse order to undo already completed steps.
				while (!completed.isEmpty()) {
					SagaStep step = completed.pop();
					context.addEvent("Compensating step: " + step.name());
					step.compensate(context);
				}

				context.addEvent("Saga compensated after failure.");
				return new SagaResult(
					request.orderId(),
					SagaStatus.COMPENSATED,
					"Order workflow failed and was compensated",
					context.events()
				);
			}
		}
	}

	static class InventoryReservationStep implements SagaStep {

		@Override
		public String name() {
			return "Reserve Inventory";
		}

		@Override
		public void execute(SagaContext context) {
			context.inventoryReserved(true);
			context.addEvent(
				"Inventory reserved for quantity " + context.request().quantity()
			);
		}

		@Override
		public void compensate(SagaContext context) {
			if (!context.inventoryReserved()) {
				return;
			}

			context.inventoryReserved(false);
			context.addEvent("Inventory reservation released");
		}
	}

	static class PaymentStep implements SagaStep {

		@Override
		public String name() {
			return "Capture Payment";
		}

		@Override
		public void execute(SagaContext context) {
			if (context.request().failPayment()) {
				throw new IllegalStateException("Payment gateway rejected transaction");
			}

			context.paymentCaptured(true);
			context.addEvent("Payment captured: $" + context.request().amount());
		}

		@Override
		public void compensate(SagaContext context) {
			if (!context.paymentCaptured()) {
				return;
			}

			context.paymentCaptured(false);
			context.addEvent("Payment refunded");
		}
	}

	static class ShippingStep implements SagaStep {

		@Override
		public String name() {
			return "Create Shipment";
		}

		@Override
		public void execute(SagaContext context) {
			if (context.request().failShipping()) {
				throw new IllegalStateException("Shipping provider unavailable");
			}

			context.shipmentCreated(true);
			context.addEvent("Shipment order created");
		}

		@Override
		public void compensate(SagaContext context) {
			if (!context.shipmentCreated()) {
				return;
			}

			context.shipmentCreated(false);
			context.addEvent("Shipment request canceled");
		}
	}
}
