package com.perishable.domain.exception;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(int productId) {
        super("Product not found with id: " + productId);
    }
}
