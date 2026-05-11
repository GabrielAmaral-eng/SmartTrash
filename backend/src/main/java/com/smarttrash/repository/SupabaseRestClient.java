package com.smarttrash.repository;

import com.smarttrash.config.SupabaseProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.function.Consumer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@ConditionalOnProperty(name = "smarttrash.data-source", havingValue = "supabase")
class SupabaseRestClient {

    private static final String BEARER_PREFIX = "Bearer ";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    SupabaseRestClient(SupabaseProperties properties, RestClient.Builder builder, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.restClient = builder
                .baseUrl(properties.restUrl())
                .defaultHeader("apikey", properties.requiredPublishableKey())
                .build();
    }

    RestClient.RequestHeadersUriSpec<?> get() {
        return restClient.get();
    }

    RestClient.RequestBodyUriSpec post() {
        return restClient.post();
    }

    RestClient.RequestBodyUriSpec patch() {
        return restClient.patch();
    }

    Consumer<HttpHeaders> userHeaders() {
        var token = currentBearerToken();
        return headers -> headers.setBearerAuth(token);
    }

    String currentUserId() {
        var token = currentBearerToken();
        var parts = token.split("\\.");
        if (parts.length < 2) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Supabase user token.");
        }

        try {
            var payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            JsonNode json = objectMapper.readTree(payload);
            var subject = json.path("sub").asText();
            if (subject == null || subject.isBlank()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Supabase user token.");
            }
            return subject;
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Supabase user token.", exception);
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Could not read Supabase user token.", exception);
        }
    }

    private String currentBearerToken() {
        var attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servletAttributes)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Supabase user token is required.");
        }

        HttpServletRequest request = servletAttributes.getRequest();
        var authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX) || authorization.length() <= BEARER_PREFIX.length()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Supabase user token is required.");
        }

        return authorization.substring(BEARER_PREFIX.length());
    }
}
