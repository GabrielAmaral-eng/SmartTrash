package com.smarttrash.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarttrash.repository.InMemoryProfileRepository;
import com.smarttrash.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({AuthService.class, InMemoryProfileRepository.class})
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void loginReturnsMockTokenAndUser() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "operator@smarttrash.local",
                                "password", "demo"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", not(blankOrNullString())))
                .andExpect(jsonPath("$.user.email").value("operator@smarttrash.local"))
                .andExpect(jsonPath("$.user.name").value("Smart Trash Operator"));
    }

    @Test
    void profileReturnsCurrentUserProfile() throws Exception {
        mockMvc.perform(get("/auth/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("gabriel_41231@aluno.eseg.edu.br"))
                .andExpect(jsonPath("$.role").value("SUPER_ADMIN"));
    }

    @Test
    void superAdminCanListAndUpdateUserRoles() throws Exception {
        mockMvc.perform(get("/auth/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users").isArray());

        mockMvc.perform(patch("/auth/users/mock-viewer/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("role", "ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }
}
