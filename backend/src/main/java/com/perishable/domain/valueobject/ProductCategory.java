package com.perishable.domain.valueobject;

import java.util.Arrays;
import java.util.Optional;

public enum ProductCategory {
    MILK_PRODUCTS("Milk Products", "MILK"),
    FRUITS("Fruits", "FRUIT"),
    VEGETABLES("Vegetables", "VEGETABLE"),
    BAKERY("Bakery Products", "BAKERY");

    private final String displayName;
    private final String dbCode;

    ProductCategory(String displayName, String dbCode) {
        this.displayName = displayName;
        this.dbCode = dbCode;
    }

    public String getDisplayName() { return displayName; }
    public String getDbCode() { return dbCode; }

    public static Optional<ProductCategory> fromDbCode(String code) {
        return Arrays.stream(values())
            .filter(c -> c.dbCode.equalsIgnoreCase(code))
            .findFirst();
    }

    public static Optional<ProductCategory> fromOrdinal(int choice) {
        if (choice < 1 || choice > values().length) return Optional.empty();
        return Optional.of(values()[choice - 1]);
    }

    @Override
    public String toString() { return displayName; }
}
