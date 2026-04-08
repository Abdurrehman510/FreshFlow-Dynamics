package com.perishable.domain.service;

import com.perishable.domain.model.Product;
import com.perishable.domain.repository.OrderHistoryRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ═══════════════════════════════════════════════════════════════
 * DEMAND FORECASTING ENGINE
 * ═══════════════════════════════════════════════════════════════
 *
 * Uses 30-day Weighted Moving Average (WMA) with recency weighting.
 * Recent days are weighted more heavily than older ones.
 *
 * WHY THIS MATTERS:
 *   Most kirana stores and mess operators order by gut feeling.
 *   This engine replaces guesswork with data — "Order 15kg tomatoes
 *   by Wednesday based on your last 4 weeks of sales history."
 *
 * ALGORITHM:
 *   WMA assigns linearly increasing weights to recent data points.
 *   Day 30 (oldest) → weight 1
 *   Day 1 (most recent) → weight 30
 *   Weighted sum / sum of weights = WMA forecast
 *
 * OUTPUT:
 *   - Predicted units needed for next 7 days
 *   - Recommended reorder point and quantity
 *   - Demand trend (INCREASING, STABLE, DECREASING)
 *   - Festival/weekend spike alerts (rule-based)
 */
public class DemandForecaster {

    private static final int FORECAST_WINDOW_DAYS = 30;
    private static final int FORECAST_HORIZON_DAYS = 7;
    private static final double SPIKE_THRESHOLD = 1.5; // 50% above average = spike

    private final OrderHistoryRepository orderHistoryRepository;

    public DemandForecaster(OrderHistoryRepository orderHistoryRepository) {
        this.orderHistoryRepository = orderHistoryRepository;
    }

    public ForecastResult forecast(Product product) {
        List<DailySales> history = orderHistoryRepository.getDailySalesHistory(
            product.getId(), FORECAST_WINDOW_DAYS
        );

        if (history.isEmpty()) {
            return ForecastResult.noData(product.getId(), product.getName());
        }

        double wmaForecast = calculateWeightedMovingAverage(history);
        double dailyAverage = history.stream()
            .mapToInt(DailySales::unitsSold)
            .average()
            .orElse(0);

        int predictedNextWeek = (int) Math.ceil(wmaForecast * FORECAST_HORIZON_DAYS);
        int recommendedReorderQty = Math.max(predictedNextWeek - product.getStockQuantity(), 0);
        DemandTrend trend = calculateTrend(history);

        boolean hasSpikeRisk = wmaForecast > dailyAverage * SPIKE_THRESHOLD;
        String recommendation = buildRecommendation(
            product, predictedNextWeek, recommendedReorderQty, trend, hasSpikeRisk
        );

        return new ForecastResult(
            product.getId(),
            product.getName(),
            wmaForecast,
            predictedNextWeek,
            recommendedReorderQty,
            trend,
            hasSpikeRisk,
            recommendation
        );
    }

    public List<ForecastResult> forecastAll(List<Product> products) {
        return products.stream()
            .map(this::forecast)
            .sorted(Comparator.comparingInt(ForecastResult::recommendedReorderQty).reversed())
            .collect(Collectors.toList());
    }

    // ============ Algorithm ============

    private double calculateWeightedMovingAverage(List<DailySales> history) {
        int n = history.size();
        double weightedSum = 0;
        double weightTotal = 0;

        for (int i = 0; i < n; i++) {
            int weight = i + 1; // Most recent = highest weight
            weightedSum += history.get(i).unitsSold() * weight;
            weightTotal += weight;
        }

        return weightTotal > 0 ? weightedSum / weightTotal : 0;
    }

    private DemandTrend calculateTrend(List<DailySales> history) {
        if (history.size() < 7) return DemandTrend.STABLE;

        // Compare last 7 days average vs previous 7 days average
        double recentAvg = history.subList(0, 7).stream()
            .mapToInt(DailySales::unitsSold).average().orElse(0);
        double previousAvg = history.subList(7, Math.min(14, history.size())).stream()
            .mapToInt(DailySales::unitsSold).average().orElse(0);

        if (previousAvg == 0) return DemandTrend.STABLE;

        double changePercent = (recentAvg - previousAvg) / previousAvg * 100;
        if (changePercent > 15) return DemandTrend.INCREASING;
        if (changePercent < -15) return DemandTrend.DECREASING;
        return DemandTrend.STABLE;
    }

    private String buildRecommendation(Product product, int predictedNextWeek,
                                        int reorderQty, DemandTrend trend, boolean spikeRisk) {
        StringBuilder sb = new StringBuilder();

        if (reorderQty > 0) {
            sb.append("📦 REORDER ALERT: Order ").append(reorderQty)
              .append(" units of ").append(product.getName())
              .append(" (current stock: ").append(product.getStockQuantity()).append(")");
        } else {
            sb.append("✅ Stock sufficient for next ").append(FORECAST_HORIZON_DAYS).append(" days");
        }

        if (trend == DemandTrend.INCREASING) {
            sb.append("\n📈 Demand is INCREASING — consider ordering extra buffer stock");
        } else if (trend == DemandTrend.DECREASING) {
            sb.append("\n📉 Demand is DECREASING — reduce next order to avoid wastage");
        }

        if (spikeRisk) {
            sb.append("\n⚡ SPIKE ALERT: Demand significantly above 30-day average — check for upcoming events/festivals");
        }

        return sb.toString();
    }

    // ============ Nested Types ============

    public enum DemandTrend { INCREASING, STABLE, DECREASING }

    public record DailySales(LocalDate date, int unitsSold) {}

    public record ForecastResult(
        int productId,
        String productName,
        double dailyForecast,
        int predictedNextWeek,
        int recommendedReorderQty,
        DemandTrend trend,
        boolean hasSpikeRisk,
        String recommendation
    ) {
        public static ForecastResult noData(int productId, String productName) {
            return new ForecastResult(productId, productName, 0, 0, 0,
                DemandTrend.STABLE, false, "⚠️ Insufficient sales history (< 1 day). Forecast available after first sales.");
        }

        public boolean needsReorder() { return recommendedReorderQty > 0; }
    }
}
