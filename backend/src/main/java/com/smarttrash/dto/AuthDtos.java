package com.smarttrash.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public final class AuthDtos {
    private AuthDtos() {
    }

    public record LoginRequest(
            @Email @NotBlank String email,
            @NotBlank String password
    ) {
    }

    public record LoginResponse(
            String token,
            AuthUser user
    ) {
    }

    public record AuthUser(
            String name,
            String email,
            String role
    ) {
    }

    public record ProfileResponse(
            String id,
            String email,
            String fullName,
            String role
    ) {
    }

    public record UserListResponse(
            List<ProfileResponse> users
    ) {
    }

    public record UpdateUserRoleRequest(
            @Pattern(regexp = "SUPER_ADMIN|ADMIN|OPERATOR|VIEWER") String role
    ) {
    }
}
