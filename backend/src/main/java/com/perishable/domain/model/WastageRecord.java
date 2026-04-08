package com.perishable.domain.model;

import com.perishable.domain.valueobject.Money;
import java.time.LocalDate;

/**
 * Wastage record — captured when products expire unsold.
 * Powers the wastage analytics dashboard.
 * This is the insight that store owners actually pay for.
 */
public record WastageRecord(
    int id,
    int productId,
    String productName,
    int unitsWasted,
    Money valueWasted,
    LocalDate recordedOn,
    String reason  // "EXPIRY", "DAMAGE", "QUALITY_ISSUE"
) {
    public static WastageRecord forExpiry(int productId, String productName,
                                          int unitsWasted, Money pricePerUnit) {
        return new WastageRecord(
            0, productId, productName, unitsWasted,
            pricePerUnit.multiply(unitsWasted), LocalDate.now(), "EXPIRY"
        );
    }
}
