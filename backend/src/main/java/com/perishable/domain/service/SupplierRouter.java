package com.perishable.domain.service;

import com.perishable.domain.model.Supplier;
import com.perishable.domain.valueobject.ProductCategory;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * ═══════════════════════════════════════════════════════════════
 * SUPPLIER ROUTING ENGINE
 * ═══════════════════════════════════════════════════════════════
 *
 * Replaces the naive "show all suppliers" with intelligent routing.
 *
 * ROUTING ALGORITHM:
 *   Composite score = (reliabilityScore * 0.6) + (speedScore * 0.4)
 *   speedScore = 1.0 - (avgDeliveryHours / 48.0)  [normalized to 48h max]
 *
 *   Rationale: For perishables, reliability matters more than speed
 *   (a reliable 24h supplier beats an unreliable 6h supplier).
 *   The 60/40 split reflects this business reality.
 */
public class SupplierRouter {

    private static final double RELIABILITY_WEIGHT = 0.6;
    private static final double SPEED_WEIGHT = 0.4;
    private static final double MAX_DELIVERY_HOURS = 48.0;

    /**
     * Returns the best supplier for a given category.
     * "Best" = highest composite score of reliability + speed.
     */
    public Optional<Supplier> findBestSupplier(List<Supplier> suppliers, ProductCategory category) {
        return suppliers.stream()
            .filter(Supplier::isActive)
            .filter(s -> s.getCategory() == category)
            .max(Comparator.comparingDouble(this::compositeScore));
    }

    /**
     * Returns all suppliers for a category, ranked by composite score.
     */
    public List<RankedSupplier> rankSuppliers(List<Supplier> suppliers, ProductCategory category) {
        return suppliers.stream()
            .filter(Supplier::isActive)
            .filter(s -> s.getCategory() == category)
            .map(s -> new RankedSupplier(s, compositeScore(s), explainScore(s)))
            .sorted(Comparator.comparingDouble(RankedSupplier::score).reversed())
            .toList();
    }

    private double compositeScore(Supplier supplier) {
        double reliabilityScore = supplier.getReliabilityScore();
        double speedScore = Math.max(0, 1.0 - (supplier.getAvgDeliveryTimeHours() / MAX_DELIVERY_HOURS));
        return (reliabilityScore * RELIABILITY_WEIGHT) + (speedScore * SPEED_WEIGHT);
    }

    private String explainScore(Supplier supplier) {
        return String.format(
            "Reliability: %.0f%% | Avg delivery: %.0fh | Score: %.2f/1.00",
            supplier.getReliabilityScore() * 100,
            supplier.getAvgDeliveryTimeHours(),
            compositeScore(supplier)
        );
    }

    public record RankedSupplier(Supplier supplier, double score, String explanation) {}
}
