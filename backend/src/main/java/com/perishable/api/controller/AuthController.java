package com.perishable.api.controller;

import com.perishable.api.dto.Dtos.*;
import com.perishable.application.usecase.AuthenticateUserUseCase;
import com.perishable.application.usecase.RegisterUserUseCase;
import com.perishable.domain.model.User;
import com.perishable.security.JwtTokenProvider;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RegisterUserUseCase registerUser;
    private final AuthenticateUserUseCase authenticateUser;
    private final JwtTokenProvider jwtProvider;

    public AuthController(RegisterUserUseCase registerUser,
                          AuthenticateUserUseCase authenticateUser,
                          JwtTokenProvider jwtProvider) {
        this.registerUser = registerUser;
        this.authenticateUser = authenticateUser;
        this.jwtProvider = jwtProvider;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest req) {
        User user = authenticateUser.execute(req.username(), req.password());
        String token = jwtProvider.generateToken(user.getUsername(), user.getRole().name());
        return ResponseEntity.ok(ApiResponse.ok(
            new AuthResponse(token, user.getUsername(), user.getRole().name(),
                user.getWalletBalance().amount().doubleValue())
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest req) {
        User.Role role = User.Role.CUSTOMER;
        if ("STORE_ADMIN".equalsIgnoreCase(req.role())) role = User.Role.STORE_ADMIN;

        User user = registerUser.execute(new RegisterUserUseCase.RegisterRequest(
            req.username(), req.password(), role
        ));
        String token = jwtProvider.generateToken(user.getUsername(), user.getRole().name());
        return ResponseEntity.ok(ApiResponse.ok("Registration successful",
            new AuthResponse(token, user.getUsername(), user.getRole().name(), 0.0)
        ));
    }
}
