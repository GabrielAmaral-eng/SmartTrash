package com.smarttrash.controller;

import com.smarttrash.dto.CollectionDtos.CollectionAssignmentResponse;
import com.smarttrash.dto.CollectionDtos.CollectionListResponse;
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

    public CollectionController(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    @GetMapping
    public CollectionListResponse collections() {
        return collectionService.listCollections();
    }

    @PostMapping("/allocations/{sensorId}")
    public CollectionAssignmentResponse allocateTeam(@PathVariable String sensorId) {
        return collectionService.allocateTeam(sensorId);
    }
}
