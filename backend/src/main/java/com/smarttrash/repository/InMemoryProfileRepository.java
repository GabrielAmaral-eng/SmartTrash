package com.smarttrash.repository;

import com.smarttrash.dto.AuthDtos.ProfileResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@ConditionalOnProperty(name = "smarttrash.data-source", havingValue = "memory", matchIfMissing = true)
public class InMemoryProfileRepository implements ProfileRepository {

    private final ConcurrentHashMap<String, ProfileResponse> profiles = new ConcurrentHashMap<>();

    public InMemoryProfileRepository() {
        profiles.put("mock-super-admin", new ProfileResponse(
                "mock-super-admin",
                "gabriel_41231@aluno.eseg.edu.br",
                "Gabriel",
                "SUPER_ADMIN"
        ));
        profiles.put("mock-operator", new ProfileResponse(
                "mock-operator",
                "operator@smarttrash.local",
                "Smart Trash Operator",
                "OPERATOR"
        ));
        profiles.put("mock-viewer", new ProfileResponse(
                "mock-viewer",
                "viewer@smarttrash.local",
                "Visualizador",
                "VIEWER"
        ));
    }

    @Override
    public Optional<ProfileResponse> findCurrentProfile() {
        return Optional.ofNullable(profiles.get("mock-super-admin"));
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
