package com.teamfourpixels.controller;

import com.teamfourpixels.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/metrics")
@RequiredArgsConstructor
@Tag(name = "Admin Metrics", description = "API для получения бизнес-метрик (только для администраторов)")
public class AdminController {

    private final UserRepository userRepository;

    @GetMapping("/summary")
    @Operation(summary = "Сводные метрики пользователей", description = "Возвращает общее количество пользователей и DAU (Daily Active Users)")
    public ResponseEntity<Map<String, Object>> getAdminMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalUsers", userRepository.count());
        metrics.put("DAU", userRepository.countActiveUsersSince(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS)));
        return ResponseEntity.ok(metrics);
    }
}
