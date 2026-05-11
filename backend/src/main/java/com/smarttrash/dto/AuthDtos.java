package com.smarttrash.dto;

import jakarta.validation.constraints.Pattern;

import java.util.List;

public final class AuthDtos {
    private AuthDtos() {
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
