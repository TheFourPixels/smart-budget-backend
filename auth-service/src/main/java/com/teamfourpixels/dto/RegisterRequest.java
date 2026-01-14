package com.teamfourpixels.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Запрос на регистрацию нового пользователя")
public class RegisterRequest {

    @Email
    @NotBlank
    @Schema(description = "Email пользователя", example = "user@example.com")
    private String email;

    @NotBlank
    @Size(min = 6)
    @Schema(description = "Пароль (минимум 6 символов)", example = "password123")
    private String password;

    @NotBlank
    @Schema(description = "Имя пользователя", example = "Иван Иванов")
    private String name;
}