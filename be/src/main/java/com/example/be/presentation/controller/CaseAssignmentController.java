package com.example.be.presentation.controller;

import com.example.be.application.dto.CaseAssignmentRequest;
import com.example.be.application.dto.CaseAssignmentResponse;
import com.example.be.application.usecase.AssignCaseUseCase;
import com.example.be.application.util.MessageUtils;
import com.example.be.domain.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/cases/{id}/assignments")
public class CaseAssignmentController {

    private final AssignCaseUseCase assignCaseUseCase;

    public CaseAssignmentController(AssignCaseUseCase assignCaseUseCase) {
        this.assignCaseUseCase = assignCaseUseCase;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> assignUsersToCase(
            @PathVariable Long id,
            @RequestBody @Valid List<CaseAssignmentRequest> requests,
            Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        try {
            List<CaseAssignmentResponse> responses = assignCaseUseCase.assignUsersToCase(id, requests, currentUser);
            Map<String, Object> response = new HashMap<>();
            response.put("status", 200);
            response.put("message", MessageUtils.getMessage("SUCCESS", "Phân công thành công"));
            response.put("data", responses);
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

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAssignments(
            @PathVariable Long id,
            Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        try {
            List<CaseAssignmentResponse> responses = assignCaseUseCase.getAssignments(id, currentUser);
            System.out.println("DEBUG: Fetched " + responses.size() + " assignments for caseId " + id);
            Map<String, Object> response = new HashMap<>();
            response.put("status", 200);
            response.put("message", MessageUtils.getMessage("SUCCESS", "Thành công"));
            response.put("data", responses);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", 400);
            error.put("message", MessageUtils.getMessage("UNKNOWN", e.getMessage()));
            return ResponseEntity.badRequest().body(error);
        }
    }

    @DeleteMapping("/{assigneeId}")
    public ResponseEntity<Map<String, Object>> removeAssignmentFromCase(
            @PathVariable Long id,
            @PathVariable Long assigneeId,
            Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        try {
            assignCaseUseCase.removeAssignmentFromCase(id, assigneeId, currentUser);
            Map<String, Object> response = new HashMap<>();
            response.put("status", 200);
            response.put("message", MessageUtils.getMessage("SUCCESS", "Xóa phân công thành công"));
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
