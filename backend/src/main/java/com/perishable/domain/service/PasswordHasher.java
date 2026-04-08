package com.perishable.domain.service;

/**
 * Domain-level password hashing interface.
 * The domain says "I need secure passwords" — infrastructure decides HOW (BCrypt).
 * This is the Dependency Inversion Principle in action.
 */
public interface PasswordHasher {
    String hash(String plainTextPassword);
    boolean verify(String plainTextPassword, String hash);
}
