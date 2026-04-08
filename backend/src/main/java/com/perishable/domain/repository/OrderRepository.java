package com.perishable.domain.repository;

import com.perishable.domain.model.Order;
import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(int id);
    List<Order> findByUserId(int userId);
    List<Order> findAll();
    void updateStatus(int orderId, Order.OrderStatus status);
}
