package com.perishable.infrastructure.security;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.perishable.domain.service.PasswordHasher;

/**
 * BCrypt implementation of PasswordHasher.
 * Cost factor 12 = ~250ms per hash (frustrates brute force attacks).
 * Industry standard: never store plain text passwords.
 *
 * This lives in infrastructure — the domain only knows the interface.
 */
public class BCryptPasswordHasher implements PasswordHasher {

    private static final int COST_FACTOR = 12;

    @Override
    public String hash(String plainTextPassword) {
        validatePassword(plainTextPassword);
        return BCrypt.withDefaults().hashToString(COST_FACTOR, plainTextPassword.toCharArray());
    }

    @Override
    public boolean verify(String plainTextPassword, String hash) {
        if (plainTextPassword == null || hash == null) return false;
        BCrypt.Result result = BCrypt.verifyer().verify(plainTextPassword.toCharArray(), hash);
        return result.verified;
    }

    private void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be blank");
        }
        if (password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }
        if (password.length() > 72) {
            throw new IllegalArgumentException("Password cannot exceed 72 characters (BCrypt limitation)");
        }

        boolean hasUpper = false, hasLower = false, hasDigit = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            if (Character.isLowerCase(c)) hasLower = true;
            if (Character.isDigit(c)) hasDigit = true;
        }
        if (!hasUpper || !hasLower || !hasDigit) {
            throw new IllegalArgumentException(
                "Password must contain at least one uppercase letter, one lowercase letter, and one digit"
            );
        }
    }
}
