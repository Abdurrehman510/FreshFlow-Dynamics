package com.perishable.application.usecase;

import com.perishable.domain.model.User;
import com.perishable.domain.repository.UserRepository;
import com.perishable.domain.service.PasswordHasher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Use Case: Authenticate User
 *
 * Validates credentials using BCrypt verify — timing-safe comparison.
 * Records last login timestamp on success.
 * NEVER reveals whether username or password was wrong (security best practice).
 */
public class AuthenticateUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(AuthenticateUserUseCase.class);
    // Generic message — don't reveal which field was wrong
    private static final String INVALID_CREDENTIALS_MSG = "Invalid username or password";

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public AuthenticateUserUseCase(UserRepository userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    public User execute(String username, String plainTextPassword) {
        if (username == null || plainTextPassword == null) {
            throw new IllegalArgumentException(INVALID_CREDENTIALS_MSG);
        }

        Optional<User> userOpt = userRepository.findByUsername(username.trim());

        // Always run BCrypt verify even if user not found — prevents timing attacks
        // that would reveal whether a username exists
        String hashToVerify = userOpt.map(User::getPasswordHash).orElse("$2a$12$dummy.hash.to.prevent.timing.attack");
        boolean passwordMatches = passwordHasher.verify(plainTextPassword, hashToVerify);

        if (userOpt.isEmpty() || !passwordMatches) {
            log.warn("Failed login attempt for username: {}", username);
            throw new IllegalArgumentException(INVALID_CREDENTIALS_MSG);
        }

        User user = userOpt.get();
        user.recordLogin();
        userRepository.save(user);

        log.info("User '{}' logged in successfully", user.getUsername());
        return user;
    }
}
