package com.perishable.api.controller;

import com.perishable.api.dto.Dtos.*;
import com.perishable.domain.model.Product;
import com.perishable.domain.repository.ProductRepository;
import com.perishable.domain.service.ExpiryPricingEngine;
import com.perishable.domain.valueobject.ExpiryDate;
import com.perishable.domain.valueobject.Money;
import com.perishable.domain.valueobject.ProductCategory;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductRepository productRepo;
    private final ExpiryPricingEngine pricingEngine;

    public ProductController(ProductRepository productRepo, ExpiryPricingEngine pricingEngine) {
        this.productRepo = productRepo;
        this.pricingEngine = pricingEngine;
    }

    // ── Public endpoints ──────────────────────────────────────

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getAvailable() {
        List<ProductResponse> products = productRepo.findAvailable()
            .stream().map(this::toResponse).toList();
        return ResponseEntity.ok(ApiResponse.ok(products));
    }

    @GetMapping("/deals")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getDeals() {
        List<ProductResponse> deals = productRepo.findAvailable().stream()
            .filter(Product::hasExpiryDiscount)
            .sorted((a, b) -> Integer.compare(b.getDiscountPercentage(), a.getDiscountPercentage()))
            .map(this::toResponse).toList();
        return ResponseEntity.ok(ApiResponse.ok(deals));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> search(@RequestParam String q) {
        List<ProductResponse> results = productRepo.searchByNameOrDescription(q)
            .stream().map(this::toResponse).toList();
        return ResponseEntity.ok(ApiResponse.ok(results));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> byCategory(@PathVariable String category) {
        ProductCategory cat = ProductCategory.fromDbCode(category)
            .orElseThrow(() -> new IllegalArgumentException("Invalid category: " + category));
        List<ProductResponse> products = productRepo.findByCategory(cat)
            .stream().filter(Product::isAvailable).map(this::toResponse).toList();
        return ResponseEntity.ok(ApiResponse.ok(products));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getById(@PathVariable int id) {
        Product p = productRepo.findById(id)
            .orElseThrow(() -> new com.perishable.domain.exception.ProductNotFoundException(id));
        return ResponseEntity.ok(ApiResponse.ok(toResponse(p)));
    }

    // ── Admin endpoints ───────────────────────────────────────

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('STORE_ADMIN','PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getAllAdmin() {
        List<ProductResponse> products = productRepo.findAll()
            .stream().map(this::toResponse).toList();
        return ResponseEntity.ok(ApiResponse.ok(products));
    }

    @GetMapping("/expiring")
    @PreAuthorize("hasAnyRole('STORE_ADMIN','PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getExpiringSoon(
            @RequestParam(defaultValue = "3") int withinDays) {
        List<ProductResponse> products = productRepo.findExpiringSoon(withinDays)
            .stream().map(this::toResponse).toList();
        return ResponseEntity.ok(ApiResponse.ok(products));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('STORE_ADMIN','PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> create(@Valid @RequestBody CreateProductRequest req) {
        ProductCategory cat = ProductCategory.fromDbCode(req.category())
            .orElseThrow(() -> new IllegalArgumentException("Invalid category: " + req.category()));

        Product product = Product.createNew(req.name(), req.description(),
            Money.of(req.basePrice()), cat,
            ExpiryDate.of(req.expiryDate()), req.stockQuantity(), req.supplierId());

        Money dynamicPrice = pricingEngine.calculatePrice(product);
        product.applyDynamicPrice(dynamicPrice);

        Product saved = productRepo.save(product);
        return ResponseEntity.ok(ApiResponse.ok("Product created", toResponse(saved)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('STORE_ADMIN','PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> update(
            @PathVariable int id, @RequestBody UpdateProductRequest req) {
        Product p = productRepo.findById(id)
            .orElseThrow(() -> new com.perishable.domain.exception.ProductNotFoundException(id));
        if (req.name() != null) p.setName(req.name());
        if (req.description() != null) p.setDescription(req.description());
        if (req.basePrice() != null) p.setBasePrice(Money.of(req.basePrice()));
        Product updated = productRepo.save(p);
        return ResponseEntity.ok(ApiResponse.ok("Product updated", toResponse(updated)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('STORE_ADMIN','PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable int id) {
        if (!productRepo.existsById(id))
            throw new com.perishable.domain.exception.ProductNotFoundException(id);
        productRepo.deleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("Product deleted", null));
    }

    @PostMapping("/reprice")
    @PreAuthorize("hasAnyRole('STORE_ADMIN','PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<String>> forceReprice() {
        List<Product> all = productRepo.findAll();
        int count = pricingEngine.repriceBatch(all);
        all.forEach(p -> productRepo.updateDynamicPrice(p.getId(),
            p.getCurrentDynamicPrice().amount().doubleValue()));
        return ResponseEntity.ok(ApiResponse.ok(count + " products repriced", null));
    }

    // ── Mapper ────────────────────────────────────────────────

    private ProductResponse toResponse(Product p) {
        return new ProductResponse(
            p.getId(), p.getName(), p.getDescription(),
            p.getBasePrice().amount().doubleValue(),
            p.getCurrentDynamicPrice().amount().doubleValue(),
            p.getDiscountPercentage(),
            p.getCategory().getDbCode(),
            p.getExpiryDate().value().toString(),
            p.getExpiryDate().daysUntilExpiry(),
            p.getStockQuantity(),
            p.isAvailable(),
            p.hasExpiryDiscount(),
            p.getExpiryDate().isExpiringSoon(),
            p.getExpiryDate().isCritical(),
            p.getWastageRate(),
            pricingEngine.explainPricing(p)
        );
    }
}
