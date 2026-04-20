package com.smarttrash.controller;

import com.smarttrash.dto.SensorDtos.SensorDetailResponse;
import com.smarttrash.dto.SensorDtos.SensorHistoryResponse;
import com.smarttrash.dto.SensorDtos.SensorListResponse;
import com.smarttrash.dto.SensorDtos.SensorLocationsResponse;
import com.smarttrash.service.SensorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sensors")
public class SensorController {

    private final SensorService sensorService;

    public SensorController(SensorService sensorService) {
        this.sensorService = sensorService;
    }

    @GetMapping
    public SensorListResponse sensors() {
        return sensorService.listSensors();
    }

    @GetMapping("/locations")
    public SensorLocationsResponse locations() {
        return sensorService.getLocations();
    }

    @GetMapping("/{id}")
    public SensorDetailResponse sensor(@PathVariable String id) {
        return sensorService.getSensor(id);
    }

    @GetMapping("/{id}/history")
    public SensorHistoryResponse history(@PathVariable String id) {
        return sensorService.getSensorHistory(id);
    }
}
