package com.perishable.api.controller;

import com.perishable.api.dto.Dtos.*;
import com.perishable.domain.model.Product;
import com.perishable.domain.model.Supplier;
import com.perishable.domain.repository.OrderRepository;
import com.perishable.domain.repository.ProductRepository;
import com.perishable.domain.repository.SupplierRepository;
import com.perishable.domain.repository.UserRepository;
import com.perishable.domain.service.DemandForecaster;
import com.perishable.domain.service.SupplierRouter;
import com.perishable.domain.service.WastageAnalyticsService;
import com.perishable.domain.valueobject.Money;
import com.perishable.domain.valueobject.ProductCategory;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/analytics")
@PreAuthorize("hasAnyRole('STORE_ADMIN','PLATFORM_ADMIN')")
public class AnalyticsController {

    private final ProductRepository productRepo;
    private final UserRepository userRepo;
    private final OrderRepository orderRepo;
    private final SupplierRepository supplierRepo;
    private final WastageAnalyticsService wastageService;
    private final DemandForecaster demandForecaster;
    private final SupplierRouter supplierRouter;

    public AnalyticsController(ProductRepository productRepo, UserRepository userRepo,
                                OrderRepository orderRepo, SupplierRepository supplierRepo,
                                WastageAnalyticsService wastageService,
                                DemandForecaster demandForecaster, SupplierRouter supplierRouter) {
        this.productRepo = productRepo;
        this.userRepo = userRepo;
        this.orderRepo = orderRepo;
        this.supplierRepo = supplierRepo;
        this.wastageService = wastageService;
        this.demandForecaster = demandForecaster;
        this.supplierRouter = supplierRouter;
    }

    // ── Dashboard KPI summary ─────────────────────────────────

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getDashboard() {
        List<Product> all = productRepo.findAll();
        List<Product> available = productRepo.findAvailable();

        int totalProducts = all.size();
        int expiringIn3Days = productRepo.findExpiringSoon(3).size();
        int outOfStock = (int) all.stream().filter(p -> p.getStockQuantity() == 0).count();
        int totalCustomers = userRepo.findAllCustomers().size();
        int pendingOrders = (int) orderRepo.findAll().stream()
            .filter(o -> o.getStatus().name().equals("PENDING")).count();

        WastageAnalyticsService.WastageReport wastageReport = wastageService.generateReport(
            LocalDate.now().minusDays(30), LocalDate.now());
        double wastageThisMonth = wastageReport.totalValueLost().amount().doubleValue();

        List<WastageItem> topWasters = wastageReport.topWasters().stream()
            .map(e -> new WastageItem(e.getKey(), e.getValue().amount().doubleValue()))
            .toList();

        // Category breakdown
        Map<ProductCategory, List<Product>> byCat = available.stream()
            .collect(Collectors.groupingBy(Product::getCategory));
        List<CategoryStat> categoryBreakdown = Arrays.stream(ProductCategory.values())
            .map(cat -> {
                List<Product> catProducts = byCat.getOrDefault(cat, List.of());
                double avgPrice = catProducts.stream()
                    .mapToDouble(p -> p.getCurrentDynamicPrice().amount().doubleValue())
                    .average().orElse(0);
                return new CategoryStat(cat.getDisplayName(), catProducts.size(), avgPrice);
            }).toList();

        return ResponseEntity.ok(ApiResponse.ok(new DashboardStatsResponse(
            totalProducts, expiringIn3Days, outOfStock,
            wastageThisMonth, pendingOrders, totalCustomers,
            topWasters, categoryBreakdown
        )));
    }

    // ── Wastage Report ────────────────────────────────────────

    @GetMapping("/wastage")
    public ResponseEntity<ApiResponse<WastageReportResponse>> getWastageReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        if (from == null) from = LocalDate.now().minusDays(30);
        if (to == null) to = LocalDate.now();

        WastageAnalyticsService.WastageReport report = wastageService.generateReport(from, to);
        List<WastageItem> topWasters = report.topWasters().stream()
            .map(e -> new WastageItem(e.getKey(), e.getValue().amount().doubleValue()))
            .toList();

        return ResponseEntity.ok(ApiResponse.ok(new WastageReportResponse(
            from.toString(), to.toString(),
            report.totalValueLost().amount().doubleValue(),
            report.totalUnitsWasted(), report.incidentCount(), topWasters
        )));
    }

    // ── Demand Forecast ───────────────────────────────────────

    @GetMapping("/forecast")
    public ResponseEntity<ApiResponse<List<ForecastResponse>>> getForecast() {
        List<Product> products = productRepo.findAvailable();
        List<ForecastResponse> forecasts = demandForecaster.forecastAll(products).stream()
            .map(f -> new ForecastResponse(
                f.productId(), f.productName(), f.dailyForecast(),
                f.predictedNextWeek(), f.recommendedReorderQty(),
                f.trend().name(), f.hasSpikeRisk(), f.recommendation()
            )).toList();
        return ResponseEntity.ok(ApiResponse.ok(forecasts));
    }

    // ── Supplier Management ───────────────────────────────────

    @GetMapping("/suppliers")
    public ResponseEntity<ApiResponse<List<SupplierResponse>>> getSuppliers(
            @RequestParam(required = false) String category) {
        List<Supplier> suppliers = category != null
            ? ProductCategory.fromDbCode(category)
                .map(supplierRepo::findByCategory)
                .orElse(supplierRepo.findAll())
            : supplierRepo.findAll();

        List<SupplierResponse> response = suppliers.stream()
            .map(s -> new SupplierResponse(s.getId(), s.getName(), s.getContactNumber(),
                s.getEmail(), s.getCategory().getDbCode(),
                s.getAvgDeliveryTimeHours(), s.getReliabilityScore(), s.isActive()))
            .toList();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/suppliers")
    public ResponseEntity<ApiResponse<SupplierResponse>> addSupplier(
            @Valid @RequestBody CreateSupplierRequest req) {
        ProductCategory cat = ProductCategory.fromDbCode(req.category())
            .orElseThrow(() -> new IllegalArgumentException("Invalid category: " + req.category()));
        Supplier supplier = Supplier.createNew(req.name(), req.contactNumber(), req.email(), cat);
        Supplier saved = supplierRepo.save(supplier);
        return ResponseEntity.ok(ApiResponse.ok(new SupplierResponse(
            saved.getId(), saved.getName(), saved.getContactNumber(), saved.getEmail(),
            saved.getCategory().getDbCode(), saved.getAvgDeliveryTimeHours(),
            saved.getReliabilityScore(), saved.isActive()
        )));
    }
}
