package com.smarttrash.repository;

import com.smarttrash.dto.AuthDtos.ProfileResponse;

import java.util.List;
import java.util.Optional;

public interface ProfileRepository {
    Optional<ProfileResponse> findCurrentProfile();

    List<ProfileResponse> findAll();

    ProfileResponse updateRole(String userId, String role);
}
