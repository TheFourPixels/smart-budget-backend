package com.teamfourpixels.controller;

import com.teamfourpixels.dto.DashboardResponse;
import com.teamfourpixels.service.DashboardService;
import com.teamfourpixels.util.UserContext;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Validated
public class DashboardController {
    private final DashboardService service;

    @GetMapping("/{year}/{month}")
    public DashboardResponse get(
            @PathVariable @Min(2000) @Max(2100) int year,
            @PathVariable @Min(1) @Max(12) int month) {

        return service.getDashboard(UserContext.getUserId(), year, month);
    }
}