package com.smarttrash.controller;

import com.smarttrash.dto.CollectionDtos.CollectionAssignmentResponse;
import com.smarttrash.dto.CollectionDtos.CollectionListResponse;
import com.smarttrash.dto.CollectionDtos.ScheduledRouteResponse;
import com.smarttrash.service.AuthService;
import com.smarttrash.service.CollectionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/collections")
public class CollectionController {

    private final CollectionService collectionService;
    private final AuthService authService;

    public CollectionController(CollectionService collectionService, AuthService authService) {
        this.collectionService = collectionService;
        this.authService = authService;
    }

    @GetMapping
    public CollectionListResponse collections() {
        authService.requireRoles("SUPER_ADMIN", "ADMIN", "OPERATOR");
        return collectionService.listCollections();
    }

    @GetMapping("/scheduled-route")
    public ScheduledRouteResponse scheduledRoute() {
        authService.requireRoles("SUPER_ADMIN", "ADMIN", "OPERATOR", "VIEWER");
        return collectionService.scheduledRoute();
    }

    @PostMapping("/allocations/{sensorId}")
    public CollectionAssignmentResponse allocateTeam(@PathVariable String sensorId) {
        authService.requireRoles("SUPER_ADMIN", "ADMIN", "OPERATOR");
        return collectionService.allocateTeam(sensorId);
    }
}
