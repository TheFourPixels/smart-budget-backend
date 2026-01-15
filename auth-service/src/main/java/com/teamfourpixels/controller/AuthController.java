package com.teamfourpixels.controller;

import com.teamfourpixels.dto.*;
import com.teamfourpixels.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "API для регистрации и входа")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Регистрация нового пользователя")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Вход в систему")
    public AuthResponse login(@Valid @RequestBody AuthRequest request) {
        return authService.login(request);
    }

    @PostMapping("/check-email")
    @Operation(summary = "Проверка существования email", description = "Используется мобильным приложением для определения экрана (Вход или Регистрация)")
    public CheckEmailResponse checkEmail(@Valid @RequestBody CheckEmailRequest request) {
        boolean exists = authService.isEmailRegistered(request.getEmail());
        return new CheckEmailResponse(exists);
    }
}