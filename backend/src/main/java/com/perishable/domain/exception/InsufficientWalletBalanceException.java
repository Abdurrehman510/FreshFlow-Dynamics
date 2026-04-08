package com.perishable.domain.exception;

public class InsufficientWalletBalanceException extends RuntimeException {
    public InsufficientWalletBalanceException(String message) { super(message); }
}
