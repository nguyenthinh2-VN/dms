package com.example.be.presentation.controller;

import com.example.be.application.dto.ReminderRequest;
import com.example.be.application.dto.ReminderResponse;
import com.example.be.application.usecase.ReminderUseCase;
import com.example.be.application.util.MessageUtils;
import com.example.be.domain.entity.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reminders")
public class ReminderController {

    private final ReminderUseCase reminderUseCase;

    public ReminderController(ReminderUseCase reminderUseCase) {
        this.reminderUseCase = reminderUseCase;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createReminder(
            @RequestBody @Valid ReminderRequest request,
            Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        try {
            ReminderResponse responseData = reminderUseCase.createReminder(request, currentUser);
            Map<String, Object> response = new HashMap<>();
            response.put("status", 200);
            response.put("message", MessageUtils.getMessage("SUCCESS", "Tạo nhắc hạn thành công"));
            response.put("data", responseData);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", 400);
            error.put("message", MessageUtils.getMessage("UNKNOWN", e.getMessage()));
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/upcoming")
    public ResponseEntity<Map<String, Object>> getUpcomingReminders(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        try {
            List<ReminderResponse> responseData = reminderUseCase.getUpcomingReminders(currentUser);
            Map<String, Object> response = new HashMap<>();
            response.put("status", 200);
            response.put("message", MessageUtils.getMessage("SUCCESS", "Thành công"));
            response.put("data", responseData);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", 400);
            error.put("message", MessageUtils.getMessage("UNKNOWN", e.getMessage()));
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<Map<String, Object>> completeReminder(
            @PathVariable Long id,
            Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        try {
            reminderUseCase.completeReminder(id, currentUser);
            Map<String, Object> response = new HashMap<>();
            response.put("status", 200);
            response.put("message", MessageUtils.getMessage("SUCCESS", "Đánh dấu hoàn thành nhắc hạn thành công"));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", 400);
            error.put("message", MessageUtils.getMessage("UNKNOWN", e.getMessage()));
            if (e.getMessage() != null && e.getMessage().contains("403_FORBIDDEN")) {
                error.put("status", 403);
                error.put("message", MessageUtils.getMessage("FORBIDDEN", "Không có quyền truy cập"));
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            return ResponseEntity.badRequest().body(error);
        }
    }
}
