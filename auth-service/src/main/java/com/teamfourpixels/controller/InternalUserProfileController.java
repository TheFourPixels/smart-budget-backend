package com.teamfourpixels.controller;

import com.teamfourpixels.dto.UserProfileDto;
import com.teamfourpixels.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/profile")
@RequiredArgsConstructor
public class InternalUserProfileController {
    private final UserProfileService service;

    @GetMapping("/{userId}")
    public UserProfileDto getProfile(@PathVariable Long userId) {
        return service.getProfile(userId);
    }
}
