package com.perishable.api.controller;

import com.perishable.api.dto.Dtos.*;
import com.perishable.domain.model.User;
import com.perishable.domain.repository.UserRepository;
import com.perishable.domain.valueobject.Money;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepo;

    public UserController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMe(@AuthenticationPrincipal String username) {
        User user = userRepo.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return ResponseEntity.ok(ApiResponse.ok(toResponse(user)));
    }

    @PostMapping("/me/wallet/topup")
    public ResponseEntity<ApiResponse<UserResponse>> topUp(
            @AuthenticationPrincipal String username,
            @Valid @RequestBody TopUpRequest req) {
        User user = userRepo.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.topUpWallet(Money.of(req.amount()));
        userRepo.updateWalletBalance(user.getId(), user.getWalletBalance().amount().doubleValue());
        return ResponseEntity.ok(ApiResponse.ok("Wallet updated", toResponse(user)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('STORE_ADMIN','PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllCustomers() {
        List<UserResponse> users = userRepo.findAllCustomers()
            .stream().map(this::toResponse).toList();
        return ResponseEntity.ok(ApiResponse.ok(users));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('STORE_ADMIN','PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable int id,
            @AuthenticationPrincipal String username) {
        User caller = userRepo.findByUsername(username).orElseThrow();
        if (caller.getId() == id)
            throw new IllegalArgumentException("Cannot delete your own account");
        userRepo.deleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("User deleted", null));
    }

    private UserResponse toResponse(User u) {
        return new UserResponse(u.getId(), u.getUsername(), u.getRole().name(),
            u.getWalletBalance().amount().doubleValue(),
            u.getCreatedAt() != null ? u.getCreatedAt().toString() : null,
            u.getLastLoginAt() != null ? u.getLastLoginAt().toString() : null);
    }
}
