package com.perishable.domain.valueobject;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Value Object for product expiry.
 * Encapsulates expiry business rules — days until expiry, expired state.
 */
public record ExpiryDate(LocalDate value) {

    public ExpiryDate {
        if (value == null) throw new IllegalArgumentException("Expiry date cannot be null");
    }

    public static ExpiryDate of(LocalDate date) {
        return new ExpiryDate(date);
    }

    public static ExpiryDate daysFromNow(int days) {
        return new ExpiryDate(LocalDate.now().plusDays(days));
    }

    public long daysUntilExpiry() {
        return ChronoUnit.DAYS.between(LocalDate.now(), value);
    }

    public boolean isExpired() {
        return LocalDate.now().isAfter(value);
    }

    public boolean isExpiringSoon() {
        return daysUntilExpiry() <= 3 && !isExpired();
    }

    public boolean isCritical() {
        return daysUntilExpiry() <= 1 && !isExpired();
    }

    @Override
    public String toString() {
        long days = daysUntilExpiry();
        if (isExpired()) return "EXPIRED (" + value + ")";
        if (days == 0) return "Expires TODAY (" + value + ")";
        return "Expires in " + days + " day(s) (" + value + ")";
    }
}
