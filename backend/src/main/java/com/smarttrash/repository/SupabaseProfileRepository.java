package com.smarttrash.repository;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smarttrash.dto.AuthDtos.ProfileResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@ConditionalOnProperty(name = "smarttrash.data-source", havingValue = "supabase")
public class SupabaseProfileRepository implements ProfileRepository {

    private final SupabaseRestClient supabase;

    public SupabaseProfileRepository(SupabaseRestClient supabase) {
        this.supabase = supabase;
    }

    @Override
    public Optional<ProfileResponse> findCurrentProfile() {
        var rows = supabase.get()
                .uri(uri -> uri.path("/profiles")
                        .queryParam("select", "id,email,full_name,role")
                        .queryParam("id", "eq." + supabase.currentUserId())
                        .queryParam("limit", "1")
                        .build())
                .headers(supabase.userHeaders())
                .retrieve()
                .body(new ParameterizedTypeReference<List<ProfileRow>>() {});

        if (rows == null || rows.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(rows.getFirst().toResponse());
    }

    @Override
    public List<ProfileResponse> findAll() {
        var rows = supabase.get()
                .uri(uri -> uri.path("/profiles")
                        .queryParam("select", "id,email,full_name,role")
                        .queryParam("order", "email.asc")
                        .build())
                .headers(supabase.userHeaders())
                .retrieve()
                .body(new ParameterizedTypeReference<List<ProfileRow>>() {});

        return (rows == null ? List.<ProfileRow>of() : rows).stream()
                .map(ProfileRow::toResponse)
                .toList();
    }

    @Override
    public ProfileResponse updateRole(String userId, String role) {
        var rows = supabase.patch()
                .uri(uri -> uri.path("/profiles")
                        .queryParam("id", "eq." + userId)
                        .queryParam("select", "id,email,full_name,role")
                        .build())
                .headers(headers -> {
                    supabase.userHeaders().accept(headers);
                    headers.set("Prefer", "return=representation");
                })
                .contentType(MediaType.APPLICATION_JSON)
                .body(java.util.Map.of("role", role))
                .retrieve()
                .body(new ParameterizedTypeReference<List<ProfileRow>>() {});

        if (rows == null || rows.isEmpty()) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "User profile not found.");
        }

        return rows.getFirst().toResponse();
    }

    private record ProfileRow(
            String id,
            String email,
            @JsonProperty("full_name") String fullName,
            String role
    ) {
        ProfileResponse toResponse() {
            return new ProfileResponse(id, email, fullName, role);
        }
    }
}
