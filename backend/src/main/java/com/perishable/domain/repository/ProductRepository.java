package com.perishable.domain.repository;

import com.perishable.domain.model.Product;
import com.perishable.domain.valueobject.ProductCategory;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    Product save(Product product);
    Optional<Product> findById(int id);
    List<Product> findAll();
    List<Product> findByCategory(ProductCategory category);
    List<Product> findAvailable();               // stock > 0 AND not expired
    List<Product> findExpiringSoon(int withinDays);
    List<Product> findExpired();
    void deleteById(int id);
    void updateDynamicPrice(int productId, double newPrice);
    void updateStock(int productId, int newQuantity);
    boolean existsById(int id);
    List<Product> searchByNameOrDescription(String query);
}
