package com.teamfourpixels.controller;

import com.teamfourpixels.dto.DashboardResponse;
import com.teamfourpixels.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService service;

    @GetMapping("/{year}/{month}")
    public DashboardResponse get(@PathVariable int year, @PathVariable int month) {
        return service.getDashboard(year, month);
    }
}