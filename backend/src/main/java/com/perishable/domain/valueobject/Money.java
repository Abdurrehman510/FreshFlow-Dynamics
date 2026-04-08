package com.perishable.domain.valueobject;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Value Object representing monetary amounts.
 * Immutable, type-safe — prevents the classic "double for money" bug.
 * Industry standard: never use float/double for money calculations.
 */
public record Money(BigDecimal amount) implements Comparable<Money> {

    public static final Money ZERO = new Money(BigDecimal.ZERO);

    public Money {
        if (amount == null) throw new IllegalArgumentException("Amount cannot be null");
        if (amount.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Money cannot be negative");
        amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public static Money of(double amount) {
        return new Money(BigDecimal.valueOf(amount));
    }

    public static Money of(String amount) {
        return new Money(new BigDecimal(amount));
    }

    public Money discountBy(int percentageOff) {
        if (percentageOff < 0 || percentageOff > 100)
            throw new IllegalArgumentException("Discount percentage must be between 0 and 100");
        BigDecimal multiplier = BigDecimal.ONE.subtract(
            BigDecimal.valueOf(percentageOff).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
        );
        return new Money(amount.multiply(multiplier));
    }

    public Money add(Money other) {
        return new Money(this.amount.add(other.amount));
    }

    public Money subtract(Money other) {
        if (this.amount.compareTo(other.amount) < 0)
            throw new IllegalArgumentException("Insufficient funds: cannot subtract " + other + " from " + this);
        return new Money(this.amount.subtract(other.amount));
    }

    public Money multiply(int quantity) {
        return new Money(this.amount.multiply(BigDecimal.valueOf(quantity)));
    }

    public boolean isGreaterThanOrEqual(Money other) {
        return this.amount.compareTo(other.amount) >= 0;
    }

    public boolean isLessThan(Money other) {
        return this.amount.compareTo(other.amount) < 0;
    }

    @Override
    public int compareTo(Money other) {
        return this.amount.compareTo(other.amount);
    }

    @Override
    public String toString() {
        return "₹" + amount.toPlainString();
    }
}
