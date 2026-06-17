package com.example.be.presentation.exception;

import com.example.be.domain.exception.InvalidRoleException;
import com.example.be.domain.exception.UserAlreadyExistsException;
import com.example.be.application.util.MessageUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        Map<String, Object> response = new HashMap<>();
        response.put("status", 400);
        response.put("message", MessageUtils.getMessage("VALIDATION_FAILED", "Validation failed"));
        response.put("errors", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, Object> response = new HashMap<>();
        // We use 401 Unauthorized for login failures per typical convention, 
        // but for mismatching passwords during registration it could be 400.
        HttpStatus status = ex.getMessage().contains("không chính xác") ? HttpStatus.UNAUTHORIZED : HttpStatus.BAD_REQUEST;
        
        response.put("status", status.value());
        response.put("message", MessageUtils.getMessage("UNKNOWN", ex.getMessage()));

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(com.example.be.domain.exception.ForbiddenException.class)
    public ResponseEntity<Map<String, Object>> handleForbiddenException(com.example.be.domain.exception.ForbiddenException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", 403);
        response.put("message", MessageUtils.getMessage("FORBIDDEN", ex.getMessage()));
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler({UserAlreadyExistsException.class, InvalidRoleException.class})
    public ResponseEntity<Map<String, Object>> handleDomainExceptions(RuntimeException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", 400);
        response.put("message", MessageUtils.getMessage("UNKNOWN", ex.getMessage()));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", 500);
        response.put("message", MessageUtils.getMessage("INTERNAL_ERROR", "Internal server error") + ": " + ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
