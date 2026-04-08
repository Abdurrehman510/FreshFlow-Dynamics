package com.perishable.config;

import com.perishable.application.usecase.*;
import com.perishable.domain.repository.*;
import com.perishable.domain.service.*;
import com.perishable.infrastructure.persistence.*;
import com.perishable.infrastructure.scheduler.NightlyPricingScheduler;
import com.perishable.infrastructure.security.BCryptPasswordHasher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class AppBeansConfig {

    // ── Repositories ──────────────────────────────────────────
    @Bean public UserRepository userRepository(DataSource ds) { return new MySqlUserRepository(ds); }
    @Bean public ProductRepository productRepository(DataSource ds) { return new MySqlProductRepository(ds); }
    @Bean public OrderRepository orderRepository(DataSource ds) { return new MySqlOrderRepository(ds); }
    @Bean public SupplierRepository supplierRepository(DataSource ds) { return new MySqlSupplierRepository(ds); }
    @Bean public WastageRepository wastageRepository(DataSource ds) { return new MySqlWastageRepository(ds); }
    @Bean public OrderHistoryRepository orderHistoryRepository(DataSource ds) { return new MySqlOrderHistoryRepository(ds); }

    // ── Domain Services ───────────────────────────────────────
    @Bean public ExpiryPricingEngine expiryPricingEngine() { return new ExpiryPricingEngine(); }
    @Bean public DemandForecaster demandForecaster(OrderHistoryRepository r) { return new DemandForecaster(r); }
    @Bean public WastageAnalyticsService wastageAnalyticsService(WastageRepository r) { return new WastageAnalyticsService(r); }
    @Bean public SupplierRouter supplierRouter() { return new SupplierRouter(); }
    @Bean public PasswordHasher passwordHasher() { return new BCryptPasswordHasher(); }

    // ── Application Use Cases ─────────────────────────────────
    @Bean public RegisterUserUseCase registerUserUseCase(UserRepository r, PasswordHasher p) {
        return new RegisterUserUseCase(r, p);
    }

    @Bean public AuthenticateUserUseCase authenticateUserUseCase(UserRepository r, PasswordHasher p) {
        return new AuthenticateUserUseCase(r, p);
    }

    @Bean public PlaceOrderUseCase placeOrderUseCase(ProductRepository pr, OrderRepository or, UserRepository ur) {
        return new PlaceOrderUseCase(pr, or, ur);
    }

    // ── Infrastructure Services ───────────────────────────────
    @Bean(initMethod = "start", destroyMethod = "stop")
    public NightlyPricingScheduler nightlyPricingScheduler(ProductRepository pr, ExpiryPricingEngine pe, WastageAnalyticsService ws) {
        return new NightlyPricingScheduler(pr, pe, ws);
    }
}
