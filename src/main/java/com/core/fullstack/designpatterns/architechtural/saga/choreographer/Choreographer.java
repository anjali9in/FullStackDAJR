package com.core.fullstack.designpatterns.architechtural.saga.choreographer;

import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class Choreographer {

	public static void main(String[] args) {
		SagaChoreography saga = new SagaChoreography();

		String successOrderId = "ORD-" + UUID.randomUUID();
		SagaResult success = saga.start(
			new OrderRequest(successOrderId, 2, 799.0, false, false, false)
		);
		printResult(success);

		String failureOrderId = "ORD-" + UUID.randomUUID();
		SagaResult failed = saga.start(
			new OrderRequest(failureOrderId, 1, 329.0, false, false, true)
		);
		printResult(failed);
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
		IN_PROGRESS,
		COMPLETED,
		FAILED,
		COMPENSATED
	}

	enum EventType {
		ORDER_CREATED,
		INVENTORY_RESERVED,
		INVENTORY_RESERVATION_FAILED,
		PAYMENT_CAPTURED,
		PAYMENT_FAILED,
		PAYMENT_REFUNDED,
		SHIPMENT_CREATED,
		SHIPMENT_FAILED,
		INVENTORY_RELEASED,
		ORDER_COMPLETED,
		ORDER_FAILED
	}

	record SagaEvent(EventType type, String orderId, String message) {
	}

	record OrderRequest(
		String orderId,
		int quantity,
		double amount,
		boolean failInventory,
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

	static class SagaState {

		private final OrderRequest request;
		private final List<String> events = new ArrayList<>();

		private SagaStatus status = SagaStatus.IN_PROGRESS;
		private String message = "Saga in progress";

		private boolean inventoryReserved;
		private boolean paymentCaptured;
		private boolean paymentRefunded;
		private boolean inventoryReleased;

		SagaState(OrderRequest request) {
			this.request = request;
			addEvent("Saga started at " + Instant.now());
		}

		void addEvent(String eventMessage) {
			events.add(eventMessage);
		}

		OrderRequest request() {
			return request;
		}

		List<String> events() {
			return List.copyOf(events);
		}

		SagaStatus status() {
			return status;
		}

		void status(SagaStatus status) {
			this.status = status;
		}

		String message() {
			return message;
		}

		void message(String message) {
			this.message = message;
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

		boolean paymentRefunded() {
			return paymentRefunded;
		}

		void paymentRefunded(boolean paymentRefunded) {
			this.paymentRefunded = paymentRefunded;
		}

		boolean inventoryReleased() {
			return inventoryReleased;
		}

		void inventoryReleased(boolean inventoryReleased) {
			this.inventoryReleased = inventoryReleased;
		}
	}

	static class EventBus {

		private final Map<EventType, List<Consumer<SagaEvent>>> subscribers = new EnumMap<>(
			EventType.class
		);

		void subscribe(EventType type, Consumer<SagaEvent> handler) {
			subscribers.computeIfAbsent(type, ignored -> new ArrayList<>()).add(handler);
		}

		void publish(SagaEvent event) {
			List<Consumer<SagaEvent>> handlers = subscribers.getOrDefault(
				event.type(),
				List.of()
			);
			for (Consumer<SagaEvent> handler : handlers) {
				handler.accept(event);
			}
		}
	}

	static class SagaStore {

		private final Map<String, SagaState> states = new HashMap<>();

		void save(SagaState state) {
			states.put(state.request().orderId(), state);
		}

		SagaState get(String orderId) {
			SagaState state = states.get(orderId);
			if (state == null) {
				throw new IllegalStateException("No saga state found for order " + orderId);
			}
			return state;
		}
	}

	static class InventoryService {

		InventoryService(EventBus eventBus, SagaStore store) {
			eventBus.subscribe(EventType.ORDER_CREATED, event -> {
				SagaState state = store.get(event.orderId());
				state.addEvent("InventoryService received ORDER_CREATED");

				if (state.request().failInventory()) {
					state.addEvent("Inventory reservation failed");
					eventBus.publish(
						new SagaEvent(
							EventType.INVENTORY_RESERVATION_FAILED,
							event.orderId(),
							"Unable to reserve inventory"
						)
					);
					return;
				}

				state.inventoryReserved(true);
				state.addEvent(
					"Inventory reserved for quantity " + state.request().quantity()
				);
				eventBus.publish(
					new SagaEvent(
						EventType.INVENTORY_RESERVED,
						event.orderId(),
						"Inventory reserved"
					)
				);
			});

			Consumer<SagaEvent> releaseInventory = event -> {
				SagaState state = store.get(event.orderId());
				if (!state.inventoryReserved()) {
					return;
				}

				state.inventoryReserved(false);
				state.inventoryReleased(true);
				state.addEvent(
					"Inventory released as compensation after " + event.type()
				);
				eventBus.publish(
					new SagaEvent(
						EventType.INVENTORY_RELEASED,
						event.orderId(),
						"Inventory released"
					)
				);
			};

			eventBus.subscribe(EventType.PAYMENT_FAILED, releaseInventory);
			eventBus.subscribe(EventType.PAYMENT_REFUNDED, releaseInventory);
		}
	}

	static class PaymentService {

		PaymentService(EventBus eventBus, SagaStore store) {
			eventBus.subscribe(EventType.INVENTORY_RESERVED, event -> {
				SagaState state = store.get(event.orderId());
				state.addEvent("PaymentService received INVENTORY_RESERVED");

				if (state.request().failPayment()) {
					state.addEvent("Payment capture failed");
					eventBus.publish(
						new SagaEvent(
							EventType.PAYMENT_FAILED,
							event.orderId(),
							"Payment failed"
						)
					);
					return;
				}

				state.paymentCaptured(true);
				state.addEvent("Payment captured: $" + state.request().amount());
				eventBus.publish(
					new SagaEvent(
						EventType.PAYMENT_CAPTURED,
						event.orderId(),
						"Payment captured"
					)
				);
			});

			eventBus.subscribe(EventType.SHIPMENT_FAILED, event -> {
				SagaState state = store.get(event.orderId());
				if (!state.paymentCaptured()) {
					return;
				}

				state.paymentCaptured(false);
				state.paymentRefunded(true);
				state.addEvent("Payment refunded as compensation");
				eventBus.publish(
					new SagaEvent(
						EventType.PAYMENT_REFUNDED,
						event.orderId(),
						"Payment refunded"
					)
				);
			});
		}
	}

	static class ShippingService {

		ShippingService(EventBus eventBus, SagaStore store) {
			eventBus.subscribe(EventType.PAYMENT_CAPTURED, event -> {
				SagaState state = store.get(event.orderId());
				state.addEvent("ShippingService received PAYMENT_CAPTURED");

				if (state.request().failShipping()) {
					state.addEvent("Shipment creation failed");
					eventBus.publish(
						new SagaEvent(
							EventType.SHIPMENT_FAILED,
							event.orderId(),
							"Unable to create shipment"
						)
					);
					return;
				}

				state.addEvent("Shipment created");
				eventBus.publish(
					new SagaEvent(
						EventType.SHIPMENT_CREATED,
						event.orderId(),
						"Shipment created"
					)
				);
			});
		}
	}

	static class OrderService {

		OrderService(EventBus eventBus, SagaStore store) {
			eventBus.subscribe(EventType.SHIPMENT_CREATED, event -> {
				SagaState state = store.get(event.orderId());
				state.status(SagaStatus.COMPLETED);
				state.message("Order workflow completed");
				state.addEvent("Order marked COMPLETED");
				eventBus.publish(
					new SagaEvent(
						EventType.ORDER_COMPLETED,
						event.orderId(),
						"Order completed"
					)
				);
			});

			eventBus.subscribe(EventType.INVENTORY_RESERVATION_FAILED, event -> {
				SagaState state = store.get(event.orderId());
				state.status(SagaStatus.FAILED);
				state.message("Order failed before compensation stage");
				state.addEvent("Order marked FAILED due to inventory issue");
				eventBus.publish(
					new SagaEvent(
						EventType.ORDER_FAILED,
						event.orderId(),
						"Order failed"
					)
				);
			});

			eventBus.subscribe(EventType.INVENTORY_RELEASED, event -> {
				SagaState state = store.get(event.orderId());
				state.status(SagaStatus.COMPENSATED);
				state.message("Order failed and compensation completed");
				state.addEvent("Order marked COMPENSATED");
				eventBus.publish(
					new SagaEvent(
						EventType.ORDER_FAILED,
						event.orderId(),
						"Order failed with compensation"
					)
				);
			});
		}
	}

	static class SagaChoreography {

		private final EventBus eventBus = new EventBus();
		private final SagaStore store = new SagaStore();

		SagaChoreography() {
			new InventoryService(eventBus, store);
			new PaymentService(eventBus, store);
			new ShippingService(eventBus, store);
			new OrderService(eventBus, store);
		}

		SagaResult start(OrderRequest request) {
			SagaState state = new SagaState(request);
			store.save(state);

			state.addEvent("Publishing ORDER_CREATED");
			eventBus.publish(
				new SagaEvent(EventType.ORDER_CREATED, request.orderId(), "Order created")
			);

			if (state.status() == SagaStatus.IN_PROGRESS) {
				state.status(SagaStatus.FAILED);
				state.message("Order ended in unknown state");
				state.addEvent("No terminal state reached");
			}

			return new SagaResult(
				request.orderId(),
				state.status(),
				state.message(),
				state.events()
			);
		}
	}
}
