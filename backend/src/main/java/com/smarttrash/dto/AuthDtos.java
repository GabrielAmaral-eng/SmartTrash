package com.smarttrash.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

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
}
