package com.example.library_management_system.web.controller;

import com.example.library_management_system.service.StatisticsService;
import com.example.library_management_system.web.dto.ApiResponse;
import com.example.library_management_system.web.dto.StatisticsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping
    public ApiResponse<StatisticsResponse> getStatistics() {
        return ApiResponse.ok(statisticsService.getStatistics());
    }
}
