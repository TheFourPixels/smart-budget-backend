package com.teamfourpixels.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Запрос на проверку кода восстановления")
public class VerifyCodeRequest {
    @NotBlank
    @Schema(description = "Email пользователя")
    private String email;

    @NotBlank
    @Schema(description = "Код из письма", example = "123456")
    private String code;
}
