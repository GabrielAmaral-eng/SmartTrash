package com.smarttrash.service;

import com.smarttrash.dto.AuthDtos.AuthUser;
import com.smarttrash.dto.AuthDtos.LoginRequest;
import com.smarttrash.dto.AuthDtos.LoginResponse;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class AuthService {

    public LoginResponse login(LoginRequest request) {
        var rawToken = request.email() + ":smart-trash-mock";
        var token = "mock-" + Base64.getUrlEncoder().withoutPadding()
                .encodeToString(rawToken.getBytes(StandardCharsets.UTF_8));
        return new LoginResponse(token, new AuthUser("Smart Trash Operator", request.email(), "OPERATOR"));
    }
}
