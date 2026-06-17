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
        response.put("code", "VALIDATION_FAILED");
        response.put("message", MessageUtils.getMessage("VALIDATION_FAILED", "Validation failed"));
        response.put("errors", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, Object> response = new HashMap<>();
        HttpStatus status = ex.getMessage() != null && ex.getMessage().contains("không chính xác") ? HttpStatus.UNAUTHORIZED : HttpStatus.BAD_REQUEST;
        
        response.put("status", status.value());
        response.put("code", status == HttpStatus.UNAUTHORIZED ? "UNAUTHORIZED" : "BAD_REQUEST");
        response.put("message", MessageUtils.getMessage("UNKNOWN", ex.getMessage()));

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(com.example.be.domain.exception.ForbiddenException.class)
    public ResponseEntity<Map<String, Object>> handleForbiddenException(com.example.be.domain.exception.ForbiddenException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", 403);
        response.put("code", "FORBIDDEN");
        response.put("message", MessageUtils.getMessage("FORBIDDEN", ex.getMessage()));
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler({InvalidRoleException.class})
    public ResponseEntity<Map<String, Object>> handleDomainExceptions(RuntimeException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", 400);
        response.put("code", "BAD_REQUEST");
        response.put("message", MessageUtils.getMessage("UNKNOWN", ex.getMessage()));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(com.example.be.domain.exception.UserAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleUserAlreadyExistsException(com.example.be.domain.exception.UserAlreadyExistsException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", 400);
        response.put("code", "USER_ALREADY_EXISTS");
        response.put("message", MessageUtils.getMessage("USER_ALREADY_EXISTS", ex.getMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(com.example.be.domain.exception.ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(com.example.be.domain.exception.ResourceNotFoundException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", 404);
        response.put("code", "NOT_FOUND");
        response.put("message", MessageUtils.getMessage("NOT_FOUND", ex.getMessage()));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllUncaughtException(Exception ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", 500);
        response.put("code", "INTERNAL_ERROR");
        response.put("message", MessageUtils.getMessage("INTERNAL_ERROR", "Internal server error: " + ex.getMessage()));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
