package com.teamfourpixels.controller;

import com.teamfourpixels.entity.CategorizationRule;
import com.teamfourpixels.repository.CategorizationRuleRepository;
import com.teamfourpixels.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/rules")
@RequiredArgsConstructor
public class RuleController {
    private final CategorizationRuleRepository repository;

    @GetMapping
    public List<CategorizationRule> getRules() {
        return repository.findByUserId(UserContext.getUserId());
    }

    @PostMapping
    public CategorizationRule createRule(@RequestBody CategorizationRule rule) {
        rule.setUserId(UserContext.getUserId());
        return repository.save(rule);
    }

    @DeleteMapping("/{id}")
    public void deleteRule(@PathVariable Long id) {
        repository.deleteById(id);
    }
}