package com.perishable.infrastructure.scheduler;

import com.perishable.domain.model.Product;
import com.perishable.domain.repository.ProductRepository;
import com.perishable.domain.service.ExpiryPricingEngine;
import com.perishable.domain.service.WastageAnalyticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Scheduled job — runs nightly to:
 *  1. Reprice all products based on updated expiry proximity
 *  2. Record wastage for expired products still in stock
 *  3. Log summary for admin visibility
 *
 * In production: use Quartz Scheduler or Spring @Scheduled
 * For this app: Java ScheduledExecutorService suffices
 */
public class NightlyPricingScheduler {

    private static final Logger log = LoggerFactory.getLogger(NightlyPricingScheduler.class);

    private final ProductRepository productRepository;
    private final ExpiryPricingEngine pricingEngine;
    private final WastageAnalyticsService wastageService;
    private final ScheduledExecutorService scheduler;

    public NightlyPricingScheduler(ProductRepository productRepository,
                                    ExpiryPricingEngine pricingEngine,
                                    WastageAnalyticsService wastageService) {
        this.productRepository = productRepository;
        this.pricingEngine = pricingEngine;
        this.wastageService = wastageService;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "nightly-pricing-scheduler");
            t.setDaemon(true);
            return t;
        });
    }

    public void start() {
        // Run immediately on startup, then every 24 hours
        scheduler.scheduleAtFixedRate(this::runNightlyJob, 0, 24, TimeUnit.HOURS);
        log.info("Nightly pricing scheduler started — runs every 24 hours");
    }

    public void stop() {
        scheduler.shutdown();
        log.info("Nightly pricing scheduler stopped");
    }

    /**
     * Manually trigger — useful for testing and admin "force reprice" action
     */
    public void runNow() {
        runNightlyJob();
    }

    private void runNightlyJob() {
        log.info("=== NIGHTLY PRICING JOB STARTED ===");

        try {
            List<Product> allProducts = productRepository.findAll();
            int totalProducts = allProducts.size();

            // Step 1: Record wastage for expired products
            List<Product> expired = allProducts.stream()
                .filter(p -> p.getExpiryDate().isExpired() && p.getStockQuantity() > 0)
                .toList();

            int wastageRecorded = 0;
            for (Product p : expired) {
                wastageService.recordExpiry(p);
                productRepository.updateStock(p.getId(), 0);
                wastageRecorded++;
            }

            // Step 2: Reprice remaining products
            List<Product> active = allProducts.stream()
                .filter(p -> !p.getExpiryDate().isExpired())
                .toList();

            int repriced = pricingEngine.repriceBatch(active);

            // Step 3: Persist updated prices
            for (Product p : active) {
                productRepository.updateDynamicPrice(
                    p.getId(),
                    p.getCurrentDynamicPrice().amount().doubleValue()
                );
            }

            // Step 4: Log alerts for critically expiring products
            List<Product> critical = pricingEngine.findCriticallyExpiring(active);
            if (!critical.isEmpty()) {
                log.warn("⚠️  {} products expiring within 24 hours:", critical.size());
                critical.forEach(p ->
                    log.warn("   → {} (Stock: {}, Price: {})",
                        p.getName(), p.getStockQuantity(), p.getCurrentDynamicPrice())
                );
            }

            log.info("=== NIGHTLY JOB COMPLETE: {}/{} products repriced, {} wastage records created ===",
                repriced, totalProducts, wastageRecorded);

        } catch (Exception e) {
            log.error("Nightly pricing job failed: {}", e.getMessage(), e);
        }
    }
}
