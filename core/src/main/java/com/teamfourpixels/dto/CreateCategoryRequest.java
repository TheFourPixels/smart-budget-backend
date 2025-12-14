package com.teamfourpixels.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCategoryRequest {

    @NotBlank(message = "Имя не должно отсутствовать или быть пустым.")
    private String name;
}