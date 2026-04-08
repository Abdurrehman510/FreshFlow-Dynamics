package com.perishable.domain.service;

import com.perishable.domain.model.Product;
import com.perishable.domain.model.WastageRecord;
import com.perishable.domain.repository.WastageRepository;
import com.perishable.domain.valueobject.Money;
import com.perishable.domain.valueobject.ProductCategory;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ═══════════════════════════════════════════════════════════════
 * WASTAGE ANALYTICS SERVICE
 * ═══════════════════════════════════════════════════════════════
 *
 * Tracks, aggregates, and surfaces wastage insights.
 *
 * DASHBOARD OUTPUTS:
 *  - Revenue lost to wastage (₹ value)
 *  - Worst-performing products by wastage rate
 *  - Category-level wastage breakdown
 *  - Month-over-month improvement trend
 *  - "Stop carrying this product" recommendations
 *
 * This is the dashboard store owners/managers return to weekly.
 * It's also what justifies the platform subscription fee.
 */
public class WastageAnalyticsService {

    private static final double HIGH_WASTAGE_THRESHOLD = 30.0; // 30%+ = problematic

    private final WastageRepository wastageRepository;

    public WastageAnalyticsService(WastageRepository wastageRepository) {
        this.wastageRepository = wastageRepository;
    }

    /**
     * Record wastage when a product expires with remaining stock.
     */
    public WastageRecord recordExpiry(Product product) {
        if (product.getStockQuantity() <= 0) return null;

        WastageRecord record = WastageRecord.forExpiry(
            product.getId(),
            product.getName(),
            product.getStockQuantity(),
            product.getBasePrice()
        );

        wastageRepository.save(record);
        product.recordWastage();
        return record;
    }

    /**
     * Full analytics report for admin dashboard.
     */
    public WastageReport generateReport(LocalDate from, LocalDate to) {
        List<WastageRecord> records = wastageRepository.findBetween(from, to);

        Money totalValueLost = records.stream()
            .map(WastageRecord::valueWasted)
            .reduce(Money.ZERO, Money::add);

        int totalUnitsWasted = records.stream()
            .mapToInt(WastageRecord::unitsWasted)
            .sum();

        Map<String, Money> byProduct = records.stream()
            .collect(Collectors.groupingBy(
                WastageRecord::productName,
                Collectors.reducing(Money.ZERO, WastageRecord::valueWasted, Money::add)
            ));

        // Top 5 worst offenders
        List<Map.Entry<String, Money>> topWasters = byProduct.entrySet().stream()
            .sorted(Map.Entry.<String, Money>comparingByValue().reversed())
            .limit(5)
            .collect(Collectors.toList());

        return new WastageReport(from, to, totalValueLost, totalUnitsWasted, records.size(), topWasters);
    }

    /**
     * Identifies products with chronic high wastage — candidates for discontinuation.
     */
    public List<DiscontinuationCandidate> findDiscontinuationCandidates(List<Product> products) {
        return products.stream()
            .filter(p -> p.getWastageRate() >= HIGH_WASTAGE_THRESHOLD)
            .map(p -> new DiscontinuationCandidate(
                p.getId(),
                p.getName(),
                p.getCategory(),
                p.getWastageRate(),
                estimateMonthlyLoss(p)
            ))
            .sorted(Comparator.comparingDouble(DiscontinuationCandidate::wastageRate).reversed())
            .collect(Collectors.toList());
    }

    private Money estimateMonthlyLoss(Product product) {
        // Simple estimate: (unitsWasted / total days active) * 30 * basePrice
        int totalUnits = product.getUnitsSoldTotal() + product.getUnitsWastedTotal();
        if (totalUnits == 0) return Money.ZERO;
        double dailyWastage = (double) product.getUnitsWastedTotal() / Math.max(totalUnits, 1);
        int estimatedMonthlyWastedUnits = (int) (dailyWastage * 30);
        return product.getBasePrice().multiply(estimatedMonthlyWastedUnits);
    }

    // ============ Report Types ============

    public record WastageReport(
        LocalDate from,
        LocalDate to,
        Money totalValueLost,
        int totalUnitsWasted,
        int incidentCount,
        List<Map.Entry<String, Money>> topWasters
    ) {}

    public record DiscontinuationCandidate(
        int productId,
        String productName,
        ProductCategory category,
        double wastageRate,
        Money estimatedMonthlyLoss
    ) {}
}
