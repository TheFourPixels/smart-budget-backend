package com.teamfourpixels.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Запрос на вход в систему")
public class AuthRequest {

    @NotBlank
    @Schema(description = "Email", example = "user@example.com")
    private String email;

    @NotBlank
    @Schema(description = "Пароль", example = "password123")
    private String password;
}