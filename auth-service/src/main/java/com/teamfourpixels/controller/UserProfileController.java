package com.teamfourpixels.controller;

import com.teamfourpixels.dto.*;
import com.teamfourpixels.service.UserProfileService;
import com.teamfourpixels.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class UserProfileController {
    private final UserProfileService service;

    @GetMapping
    public UserProfileDto getProfile() {
        return service.getProfile(UserContext.getUserId());
    }

    @PutMapping
    public UserProfileDto updateProfile(@RequestBody UpdateProfileRequest req) {
        return service.updateProfile(UserContext.getUserId(), req);
    }

    @PostMapping("/forgot-password")
    public void forgotPassword(@RequestParam String email) {
        service.forgotPassword(email);
    }

    @PostMapping("/reset-password")
    public void resetPassword(@RequestParam String token, @RequestBody String newPassword) {
        service.resetPassword(token, newPassword);
    }
}