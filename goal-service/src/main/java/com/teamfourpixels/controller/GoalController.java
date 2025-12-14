package com.teamfourpixels.controller;

import com.teamfourpixels.dto.CreateGoalRequest;
import com.teamfourpixels.dto.GoalContributionRequest;
import com.teamfourpixels.dto.GoalDto;
import com.teamfourpixels.service.GoalService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/goals")
@RequiredArgsConstructor
@Validated
public class GoalController {
    private static final Long DUMMY_USER_ID = 1L;
    private final GoalService service;

    @PostMapping
    public GoalDto create(@Valid @RequestBody CreateGoalRequest req) {
        return service.create(DUMMY_USER_ID, req);
    }

    @GetMapping("/active")
    public List<GoalDto> listActive(
            @RequestParam @Min(2000) @Max(2100) int year,
            @RequestParam @Min(1) @Max(12) int month) {
        return service.listActive(DUMMY_USER_ID, year, month);
    }

    @GetMapping("/completed")
    public List<GoalDto> listCompleted(
            @RequestParam @Min(2000) @Max(2100) int year,
            @RequestParam @Min(1) @Max(12) int month) {
        return service.listCompleted(DUMMY_USER_ID, year, month);
    }

    @GetMapping("/{id}")
    public GoalDto get(@PathVariable Long id) {
        return service.get(DUMMY_USER_ID, id);
    }

    @PostMapping("/{id}/contribute")
    public GoalDto contribute(@PathVariable Long id,
                              @Valid @RequestBody GoalContributionRequest req) {
        return service.contribute(DUMMY_USER_ID, id, req.getAmount());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(DUMMY_USER_ID, id);
    }

    @PutMapping("/{id}")
    public GoalDto update(@PathVariable Long id,
                          @Valid @RequestBody CreateGoalRequest req) {
        return service.update(DUMMY_USER_ID, id, req);
    }
}