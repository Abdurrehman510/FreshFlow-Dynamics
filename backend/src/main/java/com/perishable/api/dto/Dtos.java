package com.perishable.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// ─── AUTH ─────────────────────────────────────────────────────────────────────

record LoginRequest(@NotBlank String username, @NotBlank String password) {}
record RegisterRequest(
    @NotBlank @Size(min=3,max=30) String username,
    @NotBlank @Size(min=8,max=72) String password,
    String role  // CUSTOMER | STORE_ADMIN
) {}
record AuthResponse(String token, String username, String role, double walletBalance) {}

// ─── PRODUCT ──────────────────────────────────────────────────────────────────

record ProductResponse(
    int id, String name, String description,
    double basePrice, double currentPrice, int discountPercent,
    String category, String expiryDate, long daysUntilExpiry,
    int stockQuantity, boolean isAvailable, boolean hasDiscount,
    boolean isExpiringSoon, boolean isCritical,
    double wastageRate, String pricingExplanation
) {}

record CreateProductRequest(
    @NotBlank String name,
    String description,
    @Positive double basePrice,
    @NotBlank String category,
    @NotNull LocalDate expiryDate,
    @Min(0) int stockQuantity,
    @Positive int supplierId
) {}

record UpdateProductRequest(
    String name, String description,
    Double basePrice, String category
) {}

// ─── ORDER ────────────────────────────────────────────────────────────────────

record PlaceOrderRequest(
    @Positive int productId,
    @Min(1) int quantity,
    @NotBlank String paymentMethod   // CASH | WALLET
) {}

record OrderLineResponse(
    int productId, String productName,
    int quantity, double pricePerUnit, double lineTotal
) {}

record OrderResponse(
    int id, double totalAmount, String paymentMethod,
    String status, String placedAt,
    List<OrderLineResponse> lines
) {}

// ─── SUPPLIER ─────────────────────────────────────────────────────────────────

record SupplierResponse(
    int id, String name, String contactNumber, String email,
    String category, double avgDeliveryHours,
    double reliabilityScore, boolean isActive, double compositeScore
) {}

record CreateSupplierRequest(
    @NotBlank String name,
    @NotBlank String contactNumber,
    String email,
    @NotBlank String category
) {}

// ─── ANALYTICS ────────────────────────────────────────────────────────────────

record WastageReportResponse(
    String from, String to,
    double totalValueLost, int totalUnitsWasted,
    int incidentCount,
    List<WastageItem> topWasters
) {}

record WastageItem(String productName, double valueLost) {}

record ForecastResponse(
    int productId, String productName,
    double dailyForecast, int predictedNextWeek,
    int recommendedReorderQty, String trend,
    boolean hasSpikeRisk, String recommendation
) {}

record DashboardStatsResponse(
    int totalProducts, int expiringIn3Days, int outOfStock,
    double totalWastageThisMonth, int pendingOrders,
    int totalCustomers, List<WastageItem> topWasters,
    List<CategoryStat> categoryBreakdown
) {}

record CategoryStat(String category, int productCount, double avgPrice) {}

// ─── USER ─────────────────────────────────────────────────────────────────────

record UserResponse(
    int id, String username, String role,
    double walletBalance, String createdAt, String lastLoginAt
) {}

record TopUpRequest(@Positive double amount) {}

// ─── GENERIC ──────────────────────────────────────────────────────────────────

@JsonInclude(JsonInclude.Include.NON_NULL)
record ApiResponse<T>(boolean success, String message, T data) {
    static <T> ApiResponse<T> ok(T data) { return new ApiResponse<>(true, null, data); }
    static <T> ApiResponse<T> ok(String msg, T data) { return new ApiResponse<>(true, msg, data); }
    static <T> ApiResponse<T> error(String msg) { return new ApiResponse<>(false, msg, null); }
}

// Make all public
public class Dtos {
    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}
    public record RegisterRequest(@NotBlank @Size(min=3,max=30) String username, @NotBlank @Size(min=8) String password, String role) {}
    public record AuthResponse(String token, String username, String role, double walletBalance) {}
    public record ProductResponse(int id, String name, String description, double basePrice, double currentPrice, int discountPercent, String category, String expiryDate, long daysUntilExpiry, int stockQuantity, boolean isAvailable, boolean hasDiscount, boolean isExpiringSoon, boolean isCritical, double wastageRate, String pricingExplanation) {}
    public record CreateProductRequest(@NotBlank String name, String description, @Positive double basePrice, @NotBlank String category, @NotNull LocalDate expiryDate, @Min(0) int stockQuantity, @Positive int supplierId) {}
    public record UpdateProductRequest(String name, String description, Double basePrice) {}
    public record PlaceOrderRequest(@Positive int productId, @Min(1) int quantity, @NotBlank String paymentMethod) {}
    public record OrderLineResponse(int productId, String productName, int quantity, double pricePerUnit, double lineTotal) {}
    public record OrderResponse(int id, double totalAmount, String paymentMethod, String status, String placedAt, List<OrderLineResponse> lines) {}
    public record SupplierResponse(int id, String name, String contactNumber, String email, String category, double avgDeliveryHours, double reliabilityScore, boolean isActive) {}
    public record CreateSupplierRequest(@NotBlank String name, @NotBlank String contactNumber, String email, @NotBlank String category) {}
    public record WastageReportResponse(String from, String to, double totalValueLost, int totalUnitsWasted, int incidentCount, List<WastageItem> topWasters) {}
    public record WastageItem(String productName, double valueLost) {}
    public record ForecastResponse(int productId, String productName, double dailyForecast, int predictedNextWeek, int recommendedReorderQty, String trend, boolean hasSpikeRisk, String recommendation) {}
    public record DashboardStatsResponse(int totalProducts, int expiringIn3Days, int outOfStock, double totalWastageThisMonth, int pendingOrders, int totalCustomers, List<WastageItem> topWasters, List<CategoryStat> categoryBreakdown) {}
    public record CategoryStat(String category, int productCount, double avgPrice) {}
    public record UserResponse(int id, String username, String role, double walletBalance, String createdAt, String lastLoginAt) {}
    public record TopUpRequest(@Positive double amount) {}
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ApiResponse<T>(boolean success, String message, T data) {
        public static <T> ApiResponse<T> ok(T data) { return new ApiResponse<>(true, null, data); }
        public static <T> ApiResponse<T> ok(String msg, T data) { return new ApiResponse<>(true, msg, data); }
        public static <T> ApiResponse<T> fail(String msg) { return new ApiResponse<>(false, msg, null); }
    }
}
