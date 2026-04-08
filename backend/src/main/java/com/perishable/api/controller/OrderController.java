package com.perishable.api.controller;

import com.perishable.api.dto.Dtos.*;
import com.perishable.application.usecase.PlaceOrderUseCase;
import com.perishable.domain.model.Order;
import com.perishable.domain.model.User;
import com.perishable.domain.repository.OrderRepository;
import com.perishable.domain.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final PlaceOrderUseCase placeOrderUseCase;
    private final OrderRepository orderRepo;
    private final UserRepository userRepo;

    public OrderController(PlaceOrderUseCase placeOrderUseCase,
                           OrderRepository orderRepo,
                           UserRepository userRepo) {
        this.placeOrderUseCase = placeOrderUseCase;
        this.orderRepo = orderRepo;
        this.userRepo = userRepo;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(
            @AuthenticationPrincipal String username,
            @Valid @RequestBody PlaceOrderRequest req) {

        User user = userRepo.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Order.PaymentMethod method;
        try {
            method = Order.PaymentMethod.valueOf(req.paymentMethod().toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid payment method. Use CASH or WALLET");
        }

        PlaceOrderUseCase.PlaceOrderResult result = placeOrderUseCase.execute(
            new PlaceOrderUseCase.PlaceOrderRequest(user.getId(), req.productId(), req.quantity(), method)
        );

        return ResponseEntity.ok(ApiResponse.ok(result.message(), toResponse(result.order())));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> myOrders(
            @AuthenticationPrincipal String username) {
        User user = userRepo.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        List<OrderResponse> orders = orderRepo.findByUserId(user.getId())
            .stream().map(this::toResponse).toList();
        return ResponseEntity.ok(ApiResponse.ok(orders));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> allOrders() {
        List<OrderResponse> orders = orderRepo.findAll()
            .stream().map(this::toResponse).toList();
        return ResponseEntity.ok(ApiResponse.ok(orders));
    }

    private OrderResponse toResponse(Order o) {
        List<OrderLineResponse> lines = o.getLines().stream().map(l ->
            new OrderLineResponse(l.productId(), l.productName(), l.quantity(),
                l.pricePerUnit().amount().doubleValue(), l.lineTotal().amount().doubleValue())
        ).toList();
        return new OrderResponse(o.getId(), o.getTotalAmount().amount().doubleValue(),
            o.getPaymentMethod().name(), o.getStatus().name(),
            o.getPlacedAt().toString(), lines);
    }
}
