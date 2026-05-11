package com.smarttrash.controller;

import com.smarttrash.dto.AuthDtos.ProfileResponse;
import com.smarttrash.dto.AuthDtos.UpdateUserRoleRequest;
import com.smarttrash.dto.AuthDtos.UserListResponse;
import com.smarttrash.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/profile")
    public ResponseEntity<ProfileResponse> profile() {
        return authService.currentProfile()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/users")
    public UserListResponse users() {
        return authService.listUsers();
    }

    @PatchMapping("/users/{userId}/role")
    public ProfileResponse updateRole(@PathVariable String userId, @Valid @RequestBody UpdateUserRoleRequest request) {
        return authService.updateUserRole(userId, request.role());
    }
}
