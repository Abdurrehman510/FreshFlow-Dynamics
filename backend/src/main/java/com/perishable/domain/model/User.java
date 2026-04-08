package com.perishable.domain.model;

import com.perishable.domain.valueobject.Money;
import com.perishable.domain.exception.InsufficientWalletBalanceException;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * User aggregate root.
 * Rich domain model — business rules live here, not in service layer.
 */
public class User {

    private final int id;
    private final String username;
    private String passwordHash;       // BCrypt hash — NEVER plain text
    private final Role role;
    private Money walletBalance;
    private final LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;

    public enum Role {
        CUSTOMER, STORE_ADMIN, PLATFORM_ADMIN
    }

    // Reconstitution constructor (from database)
    public User(int id, String username, String passwordHash, Role role,
                Money walletBalance, LocalDateTime createdAt, LocalDateTime lastLoginAt) {
        this.id = id;
        this.username = Objects.requireNonNull(username, "Username cannot be null");
        this.passwordHash = Objects.requireNonNull(passwordHash, "Password hash cannot be null");
        this.role = Objects.requireNonNull(role, "Role cannot be null");
        this.walletBalance = Objects.requireNonNull(walletBalance, "Wallet balance cannot be null");
        this.createdAt = createdAt;
        this.lastLoginAt = lastLoginAt;
    }

    // Creation constructor (new user)
    public static User createNew(String username, String passwordHash, Role role) {
        return new User(0, username, passwordHash, role, Money.of(0), LocalDateTime.now(), null);
    }

    // ============ Business Methods ============

    public void topUpWallet(Money amount) {
        if (amount.isLessThan(Money.of(1))) {
            throw new IllegalArgumentException("Minimum top-up amount is ₹1");
        }
        this.walletBalance = this.walletBalance.add(amount);
    }

    public void deductFromWallet(Money amount) {
        if (walletBalance.isLessThan(amount)) {
            throw new InsufficientWalletBalanceException(
                "Wallet balance " + walletBalance + " is insufficient for payment of " + amount
            );
        }
        this.walletBalance = this.walletBalance.subtract(amount);
    }

    public boolean canAfford(Money amount) {
        return walletBalance.isGreaterThanOrEqual(amount);
    }

    public void recordLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    public boolean isAdmin() {
        return role == Role.STORE_ADMIN || role == Role.PLATFORM_ADMIN;
    }

    public boolean isPlatformAdmin() {
        return role == Role.PLATFORM_ADMIN;
    }

    // ============ Getters (no setters — immutability enforced via business methods) ============

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public Role getRole() { return role; }
    public Money getWalletBalance() { return walletBalance; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return id == user.id;
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "', role=" + role + "}";
    }
}
