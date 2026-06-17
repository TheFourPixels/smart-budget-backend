package com.teamfourpixels.controller;

import com.teamfourpixels.dto.StrategyPriorityDto;
import com.teamfourpixels.service.StrategyPriorityService;
import com.teamfourpixels.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions/priorities")
@RequiredArgsConstructor
public class StrategyPriorityController {
    private final StrategyPriorityService service;

    @GetMapping
    public List<StrategyPriorityDto> getPriorities() {
        return service.getPriorities(UserContext.getUserId());
    }

    @PutMapping
    public void updatePriorities(@RequestBody List<StrategyPriorityDto> priorities) {
        service.updatePriorities(UserContext.getUserId(), priorities);
    }
}
