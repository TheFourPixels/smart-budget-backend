package com.teamfourpixels.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Запрос на сброс пароля")
public class ForgotPasswordRequest {
    @Email
    @NotBlank
    @Schema(description = "Email пользователя", example = "user@example.com")
    private String email;
}
