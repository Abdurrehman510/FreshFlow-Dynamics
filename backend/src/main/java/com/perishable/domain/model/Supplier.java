package com.perishable.domain.model;

import com.perishable.domain.valueobject.ProductCategory;
import java.util.Objects;

/**
 * Supplier entity — represents a supply chain partner.
 * Tracks reliability metrics for smart routing decisions.
 */
public class Supplier {

    private final int id;
    private final String name;
    private final String contactNumber;
    private final String email;
    private final ProductCategory category;
    private double avgDeliveryTimeHours;    // For routing optimization
    private double reliabilityScore;         // 0.0 - 1.0, updated from delivery history
    private boolean isActive;

    public Supplier(int id, String name, String contactNumber, String email,
                    ProductCategory category, double avgDeliveryTimeHours,
                    double reliabilityScore, boolean isActive) {
        this.id = id;
        this.name = Objects.requireNonNull(name);
        this.contactNumber = contactNumber;
        this.email = email;
        this.category = Objects.requireNonNull(category);
        this.avgDeliveryTimeHours = avgDeliveryTimeHours;
        this.reliabilityScore = reliabilityScore;
        this.isActive = isActive;
    }

    public static Supplier createNew(String name, String contactNumber,
                                     String email, ProductCategory category) {
        return new Supplier(0, name, contactNumber, email, category, 24.0, 1.0, true);
    }

    public void updateReliabilityScore(boolean deliveredOnTime) {
        // Exponential moving average for reliability
        double weight = 0.1;
        double outcome = deliveredOnTime ? 1.0 : 0.0;
        this.reliabilityScore = (1 - weight) * reliabilityScore + weight * outcome;
    }

    public void deactivate() { this.isActive = false; }
    public void activate() { this.isActive = true; }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getContactNumber() { return contactNumber; }
    public String getEmail() { return email; }
    public ProductCategory getCategory() { return category; }
    public double getAvgDeliveryTimeHours() { return avgDeliveryTimeHours; }
    public double getReliabilityScore() { return reliabilityScore; }
    public boolean isActive() { return isActive; }

    @Override
    public String toString() {
        return String.format("Supplier{id=%d, name='%s', category=%s, reliability=%.0f%%}",
            id, name, category.getDisplayName(), reliabilityScore * 100);
    }
}
