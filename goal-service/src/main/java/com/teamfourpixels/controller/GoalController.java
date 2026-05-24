package com.teamfourpixels.controller;

import com.teamfourpixels.dto.CreateGoalRequest;
import com.teamfourpixels.dto.GoalContributionRequest;
import com.teamfourpixels.dto.GoalDto;
import com.teamfourpixels.service.GoalService;
import com.teamfourpixels.util.UserContext;
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
    private final GoalService service;

    @PostMapping
    public GoalDto create(@Valid @RequestBody CreateGoalRequest req) {
        return service.create(UserContext.getUserId(), req);
    }

    @GetMapping("/active")
    public List<GoalDto> listActive(
            @RequestParam @Min(2000) @Max(2100) int year,
            @RequestParam @Min(1) @Max(12) int month) {
        return service.listActive(UserContext.getUserId(), year, month);
    }

    @GetMapping("/completed")
    public List<GoalDto> listCompleted(
            @RequestParam @Min(2000) @Max(2100) int year,
            @RequestParam @Min(1) @Max(12) int month) {
        return service.listCompleted(UserContext.getUserId(), year, month);
    }

    @GetMapping("/{id}")
    public GoalDto get(@PathVariable Long id) {
        return service.get(UserContext.getUserId(), id);
    }

    @PostMapping("/{id}/contribute")
    public GoalDto contribute(@PathVariable Long id,
                              @Valid @RequestBody GoalContributionRequest req) {
        return service.contribute(UserContext.getUserId(), id, req.getAmount());
    }


    @PostMapping("/{id}/complete-early")
    public GoalDto completeEarly(@PathVariable Long id) {
        return service.completeEarly(UserContext.getUserId(), id);
    }
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(UserContext.getUserId(), id);
    }

    @PutMapping("/{id}")
    public GoalDto update(@PathVariable Long id,
                          @Valid @RequestBody CreateGoalRequest req) {
        return service.update(UserContext.getUserId(), id, req);
    }
}