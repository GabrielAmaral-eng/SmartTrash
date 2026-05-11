package com.smarttrash.controller;

import com.smarttrash.dto.DashboardDtos.DashboardHistoryResponse;
import com.smarttrash.dto.DashboardDtos.DashboardRegionsResponse;
import com.smarttrash.dto.DashboardDtos.DashboardSummaryResponse;
import com.smarttrash.service.AuthService;
import com.smarttrash.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final AuthService authService;

    public DashboardController(DashboardService dashboardService, AuthService authService) {
        this.dashboardService = dashboardService;
        this.authService = authService;
    }

    @GetMapping("/summary")
    public DashboardSummaryResponse summary() {
        authService.requireRoles("SUPER_ADMIN", "ADMIN", "OPERATOR", "VIEWER");
        return dashboardService.getSummary();
    }

    @GetMapping("/history")
    public DashboardHistoryResponse history() {
        authService.requireRoles("SUPER_ADMIN", "ADMIN", "OPERATOR", "VIEWER");
        return dashboardService.getHistory();
    }

    @GetMapping("/regions")
    public DashboardRegionsResponse regions() {
        authService.requireRoles("SUPER_ADMIN", "ADMIN", "OPERATOR", "VIEWER");
        return dashboardService.getRegions();
    }
}
