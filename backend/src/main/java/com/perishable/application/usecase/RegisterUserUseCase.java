package com.perishable.application.usecase;

import com.perishable.domain.model.User;
import com.perishable.domain.repository.UserRepository;
import com.perishable.domain.service.PasswordHasher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Use Case: Register User
 *
 * Handles user registration with proper validation:
 *  - Username uniqueness check
 *  - Password strength enforcement (via BCryptPasswordHasher)
 *  - Password is NEVER stored plain — BCrypt hash only
 */
public class RegisterUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(RegisterUserUseCase.class);
    private static final int MIN_USERNAME_LENGTH = 3;
    private static final int MAX_USERNAME_LENGTH = 30;

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public RegisterUserUseCase(UserRepository userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    public record RegisterRequest(String username, String plainTextPassword, User.Role role) {}

    public User execute(RegisterRequest request) {
        validateUsername(request.username());

        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username '" + request.username() + "' is already taken");
        }

        // Password validation happens inside BCryptPasswordHasher.hash()
        String passwordHash = passwordHasher.hash(request.plainTextPassword());

        User newUser = User.createNew(request.username(), passwordHash, request.role());
        User saved = userRepository.save(newUser);

        log.info("New user registered: {} ({})", saved.getUsername(), saved.getRole());
        return saved;
    }

    private void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be blank");
        }
        if (username.length() < MIN_USERNAME_LENGTH) {
            throw new IllegalArgumentException("Username must be at least " + MIN_USERNAME_LENGTH + " characters");
        }
        if (username.length() > MAX_USERNAME_LENGTH) {
            throw new IllegalArgumentException("Username cannot exceed " + MAX_USERNAME_LENGTH + " characters");
        }
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException("Username can only contain letters, digits, and underscores");
        }
    }
}
