package com.teamfourpixels.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ответ о статусе регистрации email")
public class CheckEmailResponse {

    @Schema(description = "Признак регистрации пользователя с данным email", example = "true")
    private boolean isRegistered;
}