package com.smarttrash.controller;

import com.smarttrash.dto.DashboardDtos.DashboardHistoryResponse;
import com.smarttrash.dto.DashboardDtos.DashboardRegionsResponse;
import com.smarttrash.dto.DashboardDtos.DashboardSummaryResponse;
import com.smarttrash.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public DashboardSummaryResponse summary() {
        return dashboardService.getSummary();
    }

    @GetMapping("/history")
    public DashboardHistoryResponse history() {
        return dashboardService.getHistory();
    }

    @GetMapping("/regions")
    public DashboardRegionsResponse regions() {
        return dashboardService.getRegions();
    }
}
