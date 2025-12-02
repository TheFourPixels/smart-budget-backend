package com.teamfourpixels.controller;

import com.teamfourpixels.dto.CreateGoalRequest;
import com.teamfourpixels.dto.GoalContributionRequest;
import com.teamfourpixels.dto.GoalDto;
import com.teamfourpixels.service.GoalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/goals")
@RequiredArgsConstructor
public class GoalController {
    private static final Long DUMMY_USER_ID = 1L;
    private final GoalService service;

    @PostMapping
    public GoalDto create(@Valid @RequestBody CreateGoalRequest req) {
        return service.create(DUMMY_USER_ID, req);
    }

    @GetMapping
    public List<GoalDto> list() {
        return service.list(DUMMY_USER_ID);
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
}