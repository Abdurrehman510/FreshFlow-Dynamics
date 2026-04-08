package com.perishable.infrastructure.persistence;

import com.perishable.domain.model.User;
import com.perishable.domain.repository.UserRepository;
import com.perishable.domain.valueobject.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * MySQL implementation of UserRepository.
 * Uses try-with-resources for every connection — zero leaks.
 * PreparedStatements everywhere — zero SQL injection.
 */
public class MySqlUserRepository implements UserRepository {

    private static final Logger log = LoggerFactory.getLogger(MySqlUserRepository.class);
    private final DataSource dataSource;

    public MySqlUserRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public User save(User user) {
        if (user.getId() == 0) {
            return insert(user);
        } else {
            return update(user);
        }
    }

    private User insert(User user) {
        String sql = """
            INSERT INTO users (username, password_hash, role, wallet_balance, created_at)
            VALUES (?, ?, ?, ?, ?)
            """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getRole().name());
            ps.setBigDecimal(4, user.getWalletBalance().amount());
            ps.setTimestamp(5, Timestamp.valueOf(user.getCreatedAt()));
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                log.info("Registered new user: {}", user.getUsername());
                return findById(keys.getInt(1)).orElseThrow();
            }
            throw new RuntimeException("Insert failed — no generated key returned");

        } catch (SQLException e) {
            log.error("Failed to insert user: {}", e.getMessage());
            throw new RuntimeException("Failed to save user: " + e.getMessage(), e);
        }
    }

    private User update(User user) {
        String sql = "UPDATE users SET wallet_balance = ?, last_login_at = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBigDecimal(1, user.getWalletBalance().amount());
            ps.setTimestamp(2, user.getLastLoginAt() != null ? Timestamp.valueOf(user.getLastLoginAt()) : null);
            ps.setInt(3, user.getId());
            ps.executeUpdate();
            return user;

        } catch (SQLException e) {
            log.error("Failed to update user {}: {}", user.getId(), e.getMessage());
            throw new RuntimeException("Failed to update user", e);
        }
    }

    @Override
    public Optional<User> findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();

        } catch (SQLException e) {
            log.error("Failed to find user by id {}: {}", id, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();

        } catch (SQLException e) {
            log.error("Failed to find user by username: {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public List<User> findAllCustomers() {
        String sql = "SELECT * FROM users WHERE role = 'CUSTOMER' ORDER BY username";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();
            List<User> users = new ArrayList<>();
            while (rs.next()) users.add(mapRow(rs));
            return users;

        } catch (SQLException e) {
            log.error("Failed to list customers: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public void deleteById(int id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows == 0) log.warn("Delete user: no user found with id {}", id);

        } catch (SQLException e) {
            log.error("Failed to delete user {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to delete user", e);
        }
    }

    @Override
    public boolean existsByUsername(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;

        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public void updateWalletBalance(int userId, double newBalance) {
        String sql = "UPDATE users SET wallet_balance = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, newBalance);
            ps.setInt(2, userId);
            ps.executeUpdate();

        } catch (SQLException e) {
            log.error("Failed to update wallet balance: {}", e.getMessage());
            throw new RuntimeException("Failed to update wallet", e);
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        return new User(
            rs.getInt("id"),
            rs.getString("username"),
            rs.getString("password_hash"),
            User.Role.valueOf(rs.getString("role")),
            Money.of(rs.getBigDecimal("wallet_balance").doubleValue()),
            rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null,
            rs.getTimestamp("last_login_at") != null ? rs.getTimestamp("last_login_at").toLocalDateTime() : null
        );
    }
}
