package com.example.be.presentation.controller;

import com.example.be.application.dto.DashboardResponse;
import com.example.be.application.usecase.DashboardUseCase;
import com.example.be.domain.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardUseCase dashboardUseCase;

    public DashboardController(DashboardUseCase dashboardUseCase) {
        this.dashboardUseCase = dashboardUseCase;
    }

    @GetMapping("/admin-stats")
    public ResponseEntity<?> getAdminStats(Authentication authentication) {
        User currentUser = extractUser(authentication);
        try {
            DashboardResponse response = dashboardUseCase.getAdminStats(currentUser);
            return ResponseEntity.ok(new Envelope<>(200, "Thành công", response));
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("403_FORBIDDEN")) {
                return ResponseEntity.status(403).body(new Envelope<>(403, e.getMessage(), null));
            }
            return ResponseEntity.status(500).body(new Envelope<>(500, e.getMessage(), null));
        }
    }

    @GetMapping("/my-stats")
    public ResponseEntity<?> getMyStats(Authentication authentication) {
        User currentUser = extractUser(authentication);
        try {
            DashboardResponse response = dashboardUseCase.getMyStats(currentUser);
            return ResponseEntity.ok(new Envelope<>(200, "Thành công", response));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new Envelope<>(500, e.getMessage(), null));
        }
    }

    private User extractUser(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }
        // Fallback for testing, should not happen if security is configured
        User fallback = new User();
        fallback.setId(1L);
        return fallback;
    }

    static class Envelope<T> {
        public int status;
        public String message;
        public T data;

        public Envelope(int status, String message, T data) {
            this.status = status;
            this.message = message;
            this.data = data;
        }
    }
}
