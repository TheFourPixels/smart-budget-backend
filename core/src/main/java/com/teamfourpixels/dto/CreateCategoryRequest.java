package com.teamfourpixels.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Запрос на создание категории")
public class CreateCategoryRequest {

    @NotBlank
    @Schema(description = "Название категории", example = "Хобби")
    private String name;
}