package com.perishable.infrastructure.persistence;

import com.perishable.domain.repository.*;

import javax.sql.DataSource;

/**
 * Repository factory — wires all infrastructure implementations together.
 * In a Spring Boot app this would be @Bean configuration.
 * For our standalone app, this is our lightweight DI container.
 */
public class RepositoryFactory {

    private final DataSource dataSource;

    // Singletons — one instance per repo per app lifecycle
    private UserRepository userRepository;
    private ProductRepository productRepository;
    private OrderRepository orderRepository;
    private SupplierRepository supplierRepository;
    private WastageRepository wastageRepository;
    private OrderHistoryRepository orderHistoryRepository;

    public RepositoryFactory(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public UserRepository users() {
        if (userRepository == null) userRepository = new MySqlUserRepository(dataSource);
        return userRepository;
    }

    public ProductRepository products() {
        if (productRepository == null) productRepository = new MySqlProductRepository(dataSource);
        return productRepository;
    }

    public OrderRepository orders() {
        if (orderRepository == null) orderRepository = new MySqlOrderRepository(dataSource);
        return orderRepository;
    }

    public SupplierRepository suppliers() {
        if (supplierRepository == null) supplierRepository = new MySqlSupplierRepository(dataSource);
        return supplierRepository;
    }

    public WastageRepository wastage() {
        if (wastageRepository == null) wastageRepository = new MySqlWastageRepository(dataSource);
        return wastageRepository;
    }

    public OrderHistoryRepository orderHistory() {
        if (orderHistoryRepository == null) orderHistoryRepository = new MySqlOrderHistoryRepository(dataSource);
        return orderHistoryRepository;
    }
}
