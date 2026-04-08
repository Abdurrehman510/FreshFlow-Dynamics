package com.perishable.domain.repository;

import com.perishable.domain.service.DemandForecaster.DailySales;
import java.util.List;

public interface OrderHistoryRepository {
    /**
     * Returns daily sales history for a product, most recent first.
     * Used by DemandForecaster for weighted moving average calculation.
     */
    List<DailySales> getDailySalesHistory(int productId, int lastNDays);
}
