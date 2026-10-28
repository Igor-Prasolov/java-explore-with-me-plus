package ru.practicum.hit.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class AdviceController {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException e) {
        return response(HttpStatus.BAD_REQUEST, "Incorrectly made request.", e.getMessage(), List.of());
    }

    private ResponseEntity<ApiError> response(HttpStatus status, String reason, String message, List<String> errors) {
        return ResponseEntity.status(status).body(apiError(status, reason, message, errors));
    }

    private ApiError apiError(HttpStatus status, String reason, String message, List<String> errors) {
        ApiError apiError = new ApiError();
        apiError.setStatus(status.name());
        apiError.setReason(reason);
        apiError.setMessage(message);
        apiError.setTimestamp(LocalDateTime.now());
        if (!errors.isEmpty()) {
            apiError.setErrors(new ArrayList<>(errors));
        }
        return apiError;
    }
}
