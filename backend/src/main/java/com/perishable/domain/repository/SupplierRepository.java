package com.perishable.domain.repository;

import com.perishable.domain.model.Supplier;
import com.perishable.domain.valueobject.ProductCategory;

import java.util.List;
import java.util.Optional;

public interface SupplierRepository {
    Supplier save(Supplier supplier);
    Optional<Supplier> findById(int id);
    List<Supplier> findAll();
    List<Supplier> findByCategory(ProductCategory category);
    List<Supplier> findActive();
    void deleteById(int id);
}
