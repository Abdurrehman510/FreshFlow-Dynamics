package com.perishable.application.usecase;

import com.perishable.domain.exception.InsufficientWalletBalanceException;
import com.perishable.domain.exception.OutOfStockException;
import com.perishable.domain.exception.ProductNotFoundException;
import com.perishable.domain.model.Order;
import com.perishable.domain.model.Order.OrderLine;
import com.perishable.domain.model.Product;
import com.perishable.domain.model.User;
import com.perishable.domain.repository.OrderRepository;
import com.perishable.domain.repository.ProductRepository;
import com.perishable.domain.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Use Case: Place Order
 *
 * Orchestrates the full order placement flow:
 *  1. Validate product availability and stock
 *  2. Snapshot price (dynamic price at time of order)
 *  3. Process payment (wallet deduction or cash)
 *  4. Decrement stock
 *  5. Persist order with full audit trail
 *
 * Uses domain objects for all business rules — this class is pure orchestration.
 */
public class PlaceOrderUseCase {

    private static final Logger log = LoggerFactory.getLogger(PlaceOrderUseCase.class);

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public PlaceOrderUseCase(ProductRepository productRepository,
                              OrderRepository orderRepository,
                              UserRepository userRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    public record PlaceOrderRequest(int userId, int productId, int quantity, Order.PaymentMethod paymentMethod) {}

    public record PlaceOrderResult(Order order, String message) {}

    public PlaceOrderResult execute(PlaceOrderRequest request) {
        // 1. Load user
        User user = userRepository.findById(request.userId())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 2. Load product and validate
        Product product = productRepository.findById(request.productId())
            .orElseThrow(() -> new ProductNotFoundException(request.productId()));

        if (product.getExpiryDate().isExpired()) {
            throw new IllegalStateException("Cannot order '" + product.getName() + "' — product is expired");
        }

        if (product.getStockQuantity() < request.quantity()) {
            throw new OutOfStockException(product.getName(), request.quantity(), product.getStockQuantity());
        }

        // 3. Build order line — price is SNAPSHOTTED at dynamic price (not base price)
        OrderLine line = new OrderLine(
            product.getId(),
            product.getName(),
            request.quantity(),
            product.getCurrentDynamicPrice()  // Dynamic price locked in at order time
        );

        Order order = Order.createNew(request.userId(), List.of(line), request.paymentMethod());

        // 4. Process payment
        if (request.paymentMethod() == Order.PaymentMethod.WALLET) {
            user.deductFromWallet(order.getTotalAmount()); // Throws if insufficient
            userRepository.updateWalletBalance(user.getId(),
                user.getWalletBalance().amount().doubleValue());
        }

        // 5. Decrement stock in domain model
        product.recordSale(request.quantity());
        productRepository.updateStock(product.getId(), product.getStockQuantity());

        // 6. Persist order
        order.confirm();
        Order savedOrder = orderRepository.save(order);

        String message = String.format(
            "✅ Order #%d placed successfully! %d × %s @ %s = %s (%s payment)",
            savedOrder.getId(), request.quantity(), product.getName(),
            product.getCurrentDynamicPrice(), savedOrder.getTotalAmount(),
            request.paymentMethod()
        );

        // Surface discount information to customer
        if (product.hasExpiryDiscount()) {
            message += String.format("\n💰 You saved %d%% with expiry discount! (%s vs base price %s)",
                product.getDiscountPercentage(), product.getCurrentDynamicPrice(), product.getBasePrice());
        }

        log.info("Order placed: user={}, product={}, qty={}, total={}, payment={}",
            request.userId(), product.getName(), request.quantity(),
            savedOrder.getTotalAmount(), request.paymentMethod());

        return new PlaceOrderResult(savedOrder, message);
    }
}
