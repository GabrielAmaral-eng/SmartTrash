package com.smarttrash.controller;

import com.smarttrash.dto.SensorDtos.SensorDetailResponse;
import com.smarttrash.dto.SensorDtos.SensorHistoryResponse;
import com.smarttrash.dto.SensorDtos.SensorListResponse;
import com.smarttrash.dto.SensorDtos.SensorLocationsResponse;
import com.smarttrash.service.AuthService;
import com.smarttrash.service.SensorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sensors")
public class SensorController {

    private final SensorService sensorService;
    private final AuthService authService;

    public SensorController(SensorService sensorService, AuthService authService) {
        this.sensorService = sensorService;
        this.authService = authService;
    }

    @GetMapping
    public SensorListResponse sensors() {
        authService.requireRoles("SUPER_ADMIN", "ADMIN", "OPERATOR");
        return sensorService.listSensors();
    }

    @GetMapping("/locations")
    public SensorLocationsResponse locations() {
        authService.requireRoles("SUPER_ADMIN", "ADMIN", "OPERATOR", "VIEWER");
        return sensorService.getLocations();
    }

    @GetMapping("/{id}")
    public SensorDetailResponse sensor(@PathVariable String id) {
        authService.requireRoles("SUPER_ADMIN", "ADMIN", "OPERATOR");
        return sensorService.getSensor(id);
    }

    @GetMapping("/{id}/history")
    public SensorHistoryResponse history(@PathVariable String id) {
        authService.requireRoles("SUPER_ADMIN", "ADMIN", "OPERATOR");
        return sensorService.getSensorHistory(id);
    }
}
