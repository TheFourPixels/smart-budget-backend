package com.teamfourpixels.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.teamfourpixels.dto.ErrorResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(IllegalArgumentException ex) {
        ErrorResponse error = new ErrorResponse(400, ex.getMessage());
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(EntityNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(404, ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String defaultMessage = ex.getBindingResult().getFieldError().getDefaultMessage();

        ErrorResponse error = new ErrorResponse(400, defaultMessage);
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleJsonParsingError(HttpMessageNotReadableException ex) {

        Throwable cause = ex.getMostSpecificCause();

        String message;

        if (cause instanceof InvalidFormatException invalidFormatException) {

            String targetType = invalidFormatException.getTargetType() != null
                    ? invalidFormatException.getTargetType().getSimpleName()
                    : "unknown type";

            message = String.format(
                    "Ошибка формата данных: Значение '%s' не может быть преобразовано в тип %s.",
                    invalidFormatException.getValue(), targetType
            );

        } else {
            message = "Ошибка: Неверный JSON. Тело запроса не соответствует синтаксису или имеет неполную структуру.";
        }

        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return ResponseEntity.badRequest().body(error);
    }
}