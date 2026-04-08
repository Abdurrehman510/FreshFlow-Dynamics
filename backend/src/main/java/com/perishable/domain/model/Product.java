package com.perishable.domain.model;

import com.perishable.domain.valueobject.ExpiryDate;
import com.perishable.domain.valueobject.Money;
import com.perishable.domain.valueobject.ProductCategory;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Product aggregate root.
 * Key innovation: tracks expiry, stock quantity, wastage metrics.
 * This is what separates a perishables platform from a generic e-commerce app.
 */
public class Product {

    private final int id;
    private String name;
    private String description;
    private Money basePrice;             // Original price set by admin
    private Money currentDynamicPrice;  // Expiry-adjusted price (recalculated by ExpiryPricingEngine)
    private final ProductCategory category;
    private ExpiryDate expiryDate;
    private int stockQuantity;
    private int unitsSoldTotal;          // For demand forecasting
    private int unitsWastedTotal;        // For wastage analytics
    private final int supplierId;
    private final LocalDateTime addedAt;
    private LocalDateTime lastUpdatedAt;

    // Reconstitution from DB
    public Product(int id, String name, String description, Money basePrice, Money currentDynamicPrice,
                   ProductCategory category, ExpiryDate expiryDate, int stockQuantity,
                   int unitsSoldTotal, int unitsWastedTotal, int supplierId,
                   LocalDateTime addedAt, LocalDateTime lastUpdatedAt) {
        this.id = id;
        this.name = Objects.requireNonNull(name);
        this.description = description;
        this.basePrice = Objects.requireNonNull(basePrice);
        this.currentDynamicPrice = currentDynamicPrice != null ? currentDynamicPrice : basePrice;
        this.category = Objects.requireNonNull(category);
        this.expiryDate = Objects.requireNonNull(expiryDate);
        this.stockQuantity = stockQuantity;
        this.unitsSoldTotal = unitsSoldTotal;
        this.unitsWastedTotal = unitsWastedTotal;
        this.supplierId = supplierId;
        this.addedAt = addedAt;
        this.lastUpdatedAt = lastUpdatedAt;
    }

    // Factory for new product
    public static Product createNew(String name, String description, Money basePrice,
                                    ProductCategory category, ExpiryDate expiryDate,
                                    int stockQuantity, int supplierId) {
        return new Product(0, name, description, basePrice, basePrice, category,
                expiryDate, stockQuantity, 0, 0, supplierId, LocalDateTime.now(), LocalDateTime.now());
    }

    // ============ Business Methods ============

    public void recordSale(int quantity) {
        if (quantity > stockQuantity) {
            throw new IllegalStateException("Cannot sell " + quantity + " units — only " + stockQuantity + " in stock");
        }
        this.stockQuantity -= quantity;
        this.unitsSoldTotal += quantity;
        this.lastUpdatedAt = LocalDateTime.now();
    }

    public void recordWastage() {
        if (stockQuantity > 0) {
            this.unitsWastedTotal += stockQuantity;
            this.stockQuantity = 0;
            this.lastUpdatedAt = LocalDateTime.now();
        }
    }

    public void restock(int quantity, ExpiryDate newExpiryDate, Money newBasePrice) {
        this.stockQuantity += quantity;
        this.expiryDate = newExpiryDate;
        this.basePrice = newBasePrice;
        this.currentDynamicPrice = newBasePrice;
        this.lastUpdatedAt = LocalDateTime.now();
    }

    public void applyDynamicPrice(Money price) {
        this.currentDynamicPrice = price;
        this.lastUpdatedAt = LocalDateTime.now();
    }

    public boolean isAvailable() {
        return stockQuantity > 0 && !expiryDate.isExpired();
    }

    public boolean isLowStock() {
        return stockQuantity > 0 && stockQuantity <= 5;
    }

    public double getWastageRate() {
        int totalProduced = unitsSoldTotal + unitsWastedTotal;
        if (totalProduced == 0) return 0.0;
        return (double) unitsWastedTotal / totalProduced * 100.0;
    }

    public boolean hasExpiryDiscount() {
        return currentDynamicPrice.compareTo(basePrice) < 0;
    }

    public int getDiscountPercentage() {
        if (!hasExpiryDiscount()) return 0;
        var saving = basePrice.amount().subtract(currentDynamicPrice.amount());
        return saving.multiply(java.math.BigDecimal.valueOf(100))
                     .divide(basePrice.amount(), java.math.RoundingMode.HALF_UP)
                     .intValue();
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Money getBasePrice() { return basePrice; }
    public Money getCurrentDynamicPrice() { return currentDynamicPrice; }
    public ProductCategory getCategory() { return category; }
    public ExpiryDate getExpiryDate() { return expiryDate; }
    public int getStockQuantity() { return stockQuantity; }
    public int getUnitsSoldTotal() { return unitsSoldTotal; }
    public int getUnitsWastedTotal() { return unitsWastedTotal; }
    public int getSupplierId() { return supplierId; }
    public LocalDateTime getAddedAt() { return addedAt; }
    public LocalDateTime getLastUpdatedAt() { return lastUpdatedAt; }

    public void setName(String name) { this.name = name; this.lastUpdatedAt = LocalDateTime.now(); }
    public void setDescription(String description) { this.description = description; }
    public void setBasePrice(Money price) { this.basePrice = price; this.lastUpdatedAt = LocalDateTime.now(); }
}
