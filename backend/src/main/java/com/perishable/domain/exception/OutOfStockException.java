package com.perishable.domain.exception;

public class OutOfStockException extends RuntimeException {
    public OutOfStockException(String productName, int requested, int available) {
        super(String.format("'%s' has only %d units available, but %d were requested",
              productName, available, requested));
    }
}
