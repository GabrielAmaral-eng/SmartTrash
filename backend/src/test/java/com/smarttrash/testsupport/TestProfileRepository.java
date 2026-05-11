package com.smarttrash.testsupport;

import com.smarttrash.dto.AuthDtos.ProfileResponse;
import com.smarttrash.repository.ProfileRepository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class TestProfileRepository implements ProfileRepository {

    private final ConcurrentHashMap<String, ProfileResponse> profiles = new ConcurrentHashMap<>();

    public TestProfileRepository() {
        profiles.put("test-super-admin", new ProfileResponse("test-super-admin", "gabriel_41231@aluno.eseg.edu.br", "Gabriel", "SUPER_ADMIN"));
        profiles.put("test-operator", new ProfileResponse("test-operator", "operator@smarttrash.local", "Smart Trash Operator", "OPERATOR"));
        profiles.put("test-viewer", new ProfileResponse("test-viewer", "viewer@smarttrash.local", "Visualizador", "VIEWER"));
    }

    @Override
    public Optional<ProfileResponse> findCurrentProfile() {
        return Optional.ofNullable(profiles.get("test-super-admin"));
    }

    @Override
    public List<ProfileResponse> findAll() {
        return profiles.values().stream()
                .sorted((left, right) -> left.email().compareToIgnoreCase(right.email()))
                .toList();
    }

    @Override
    public ProfileResponse updateRole(String userId, String role) {
        return profiles.compute(userId, (id, existing) -> {
            if (existing == null) {
                return new ProfileResponse(id, id, null, role);
            }
            return new ProfileResponse(existing.id(), existing.email(), existing.fullName(), role);
        });
    }
}
