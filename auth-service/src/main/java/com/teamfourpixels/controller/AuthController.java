package com.teamfourpixels.controller;

import com.teamfourpixels.dto.AuthRequest;
import com.teamfourpixels.dto.AuthResponse;
import com.teamfourpixels.dto.RegisterRequest;
import com.teamfourpixels.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody AuthRequest request) {
        return authService.login(request);
    }

    @PostMapping("/check-email")
    public Map<String, Boolean> checkEmail(@RequestBody Map<String, String> request) {
        boolean exists = authService.isEmailRegistered(request.get("email"));
        return Map.of("isRegistered", exists);
    }
}