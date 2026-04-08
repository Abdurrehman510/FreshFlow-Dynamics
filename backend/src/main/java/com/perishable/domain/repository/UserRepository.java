package com.perishable.domain.repository;

import com.perishable.domain.model.User;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface — defined in domain, implemented in infrastructure.
 * Domain never knows about MySQL, JDBC, or any persistence mechanism.
 * This is the Repository Pattern from Domain Driven Design.
 */
public interface UserRepository {
    User save(User user);
    Optional<User> findById(int id);
    Optional<User> findByUsername(String username);
    List<User> findAllCustomers();
    void deleteById(int id);
    boolean existsByUsername(String username);
    void updateWalletBalance(int userId, double newBalance);
}
