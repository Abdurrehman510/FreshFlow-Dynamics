package com.perishable.application;

import com.perishable.application.usecase.*;
import com.perishable.domain.service.*;
import com.perishable.infrastructure.config.DatabaseConfig;
import com.perishable.infrastructure.persistence.RepositoryFactory;
import com.perishable.infrastructure.scheduler.NightlyPricingScheduler;
import com.perishable.infrastructure.security.BCryptPasswordHasher;

/**
 * Application-level DI container.
 *
 * Wires together all layers:
 *   Infrastructure → Domain → Application
 *
 * In a Spring Boot app, this would be replaced by @Component/@Service/@Autowired.
 * For a standalone CLI app, this manual wiring is clean and explicit.
 */
public class ApplicationContext {

    // Infrastructure
    private final RepositoryFactory repos;
    private final PasswordHasher passwordHasher;

    // Domain Services
    public final ExpiryPricingEngine pricingEngine;
    public final DemandForecaster demandForecaster;
    public final WastageAnalyticsService wastageAnalytics;
    public final SupplierRouter supplierRouter;

    // Application Use Cases
    public final RegisterUserUseCase registerUser;
    public final AuthenticateUserUseCase authenticateUser;
    public final PlaceOrderUseCase placeOrder;

    // Scheduler
    public final NightlyPricingScheduler nightlyScheduler;

    public ApplicationContext() {
        // Infrastructure layer
        this.repos = new RepositoryFactory(DatabaseConfig.getDataSource());
        this.passwordHasher = new BCryptPasswordHasher();

        // Domain services (pure domain logic — no infra deps except via interfaces)
        this.pricingEngine = new ExpiryPricingEngine();
        this.demandForecaster = new DemandForecaster(repos.orderHistory());
        this.wastageAnalytics = new WastageAnalyticsService(repos.wastage());
        this.supplierRouter = new SupplierRouter();

        // Application use cases
        this.registerUser = new RegisterUserUseCase(repos.users(), passwordHasher);
        this.authenticateUser = new AuthenticateUserUseCase(repos.users(), passwordHasher);
        this.placeOrder = new PlaceOrderUseCase(repos.products(), repos.orders(), repos.users());

        // Scheduler
        this.nightlyScheduler = new NightlyPricingScheduler(repos.products(), pricingEngine, wastageAnalytics);
    }

    // Convenient passthrough accessors
    public RepositoryFactory repos() { return repos; }
}
