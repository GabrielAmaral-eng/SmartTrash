package com.smarttrash.service;

import com.smarttrash.dto.AuthDtos.AuthUser;
import com.smarttrash.dto.AuthDtos.LoginRequest;
import com.smarttrash.dto.AuthDtos.LoginResponse;
import com.smarttrash.dto.AuthDtos.ProfileResponse;
import com.smarttrash.dto.AuthDtos.UserListResponse;
import com.smarttrash.repository.ProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Arrays;
import java.util.Optional;

@Service
public class AuthService {

    private final ProfileRepository profileRepository;

    public AuthService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    public LoginResponse login(LoginRequest request) {
        var rawToken = request.email() + ":smart-trash-mock";
        var token = "mock-" + Base64.getUrlEncoder().withoutPadding()
                .encodeToString(rawToken.getBytes(StandardCharsets.UTF_8));
        return new LoginResponse(token, new AuthUser("Smart Trash Operator", request.email(), "OPERATOR"));
    }

    public Optional<ProfileResponse> currentProfile() {
        return profileRepository.findCurrentProfile();
    }

    public UserListResponse listUsers() {
        requireRoles("SUPER_ADMIN");
        return new UserListResponse(profileRepository.findAll());
    }

    public ProfileResponse updateUserRole(String userId, String role) {
        requireRoles("SUPER_ADMIN");
        if ("SUPER_ADMIN".equals(role) && currentProfile().map(ProfileResponse::id).filter(id -> id.equals(userId)).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only the current Super-Admin can keep the Super-Admin role.");
        }
        return profileRepository.updateRole(userId, role);
    }

    public void requireRoles(String... allowedRoles) {
        var role = currentProfile()
                .map(ProfileResponse::role)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated profile is required."));

        if (Arrays.stream(allowedRoles).noneMatch(role::equals)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User role does not allow this action.");
        }
    }
}
