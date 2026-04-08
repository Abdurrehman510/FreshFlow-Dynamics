package com.perishable.domain.model;

import com.perishable.domain.valueobject.Money;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Order aggregate root.
 * Contains order lines (product + quantity + price at time of purchase).
 * Price is snapshotted at order time — dynamic pricing is locked in.
 */
public class Order {

    private final int id;
    private final int userId;
    private final List<OrderLine> lines;
    private final Money totalAmount;
    private final PaymentMethod paymentMethod;
    private OrderStatus status;
    private final LocalDateTime placedAt;

    public enum PaymentMethod { CASH, WALLET }

    public enum OrderStatus {
        PENDING, CONFIRMED, DELIVERED, CANCELLED
    }

    public record OrderLine(int productId, String productName, int quantity, Money pricePerUnit) {
        public Money lineTotal() {
            return pricePerUnit.multiply(quantity);
        }
    }

    // Reconstitution
    public Order(int id, int userId, List<OrderLine> lines, Money totalAmount,
                 PaymentMethod paymentMethod, OrderStatus status, LocalDateTime placedAt) {
        this.id = id;
        this.userId = userId;
        this.lines = new ArrayList<>(Objects.requireNonNull(lines));
        this.totalAmount = totalAmount;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.placedAt = placedAt;
    }

    // Factory for new order
    public static Order createNew(int userId, List<OrderLine> lines, PaymentMethod paymentMethod) {
        Money total = lines.stream()
            .map(OrderLine::lineTotal)
            .reduce(Money.ZERO, Money::add);
        return new Order(0, userId, lines, total, paymentMethod, OrderStatus.PENDING, LocalDateTime.now());
    }

    public void confirm() {
        if (status != OrderStatus.PENDING)
            throw new IllegalStateException("Only PENDING orders can be confirmed");
        this.status = OrderStatus.CONFIRMED;
    }

    public void markDelivered() {
        if (status != OrderStatus.CONFIRMED)
            throw new IllegalStateException("Only CONFIRMED orders can be delivered");
        this.status = OrderStatus.DELIVERED;
    }

    public void cancel() {
        if (status == OrderStatus.DELIVERED)
            throw new IllegalStateException("Delivered orders cannot be cancelled");
        this.status = OrderStatus.CANCELLED;
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public List<OrderLine> getLines() { return Collections.unmodifiableList(lines); }
    public Money getTotalAmount() { return totalAmount; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public OrderStatus getStatus() { return status; }
    public LocalDateTime getPlacedAt() { return placedAt; }
}
