package com.example.be.presentation.controller;

import com.example.be.application.dto.CaseResponse;
import com.example.be.application.dto.CreateCaseRequest;
import com.example.be.application.dto.UpdateCaseRequest;
import com.example.be.application.usecase.CaseUseCase;
import com.example.be.domain.entity.CaseStatus;
import com.example.be.domain.entity.User;
import com.example.be.domain.entity.CaseCategory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/cases")
public class CaseController {

    private final CaseUseCase caseUseCase;

    public CaseController(CaseUseCase caseUseCase) {
        this.caseUseCase = caseUseCase;
    }

    @GetMapping("/statuses")
    public ResponseEntity<Map<String, Object>> getCaseStatuses() {
        List<Map<String, String>> statuses = new ArrayList<>();
        for (CaseStatus status : CaseStatus.values()) {
            Map<String, String> map = new HashMap<>();
            map.put("code", status.name());
            map.put("description", status.getDescription());
            statuses.add(map);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", statuses);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/categories")
    public ResponseEntity<Map<String, Object>> getCaseCategories() {
        List<Map<String, String>> statuses = new ArrayList<>();
        for (CaseCategory status : CaseCategory.values()) {
            Map<String, String> map = new HashMap<>();
            map.put("code", status.name());
            map.put("description", status.getDescription());
            statuses.add(map);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", statuses);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createCase(@RequestBody CreateCaseRequest request, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        try {
            CaseResponse caseResponse = caseUseCase.createCase(request, currentUser);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", caseResponse);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            if (e.getMessage() != null && e.getMessage().contains("403_FORBIDDEN")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getCases(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        List<CaseResponse> cases = caseUseCase.getCases(currentUser);
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", cases);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getCaseDetails(@PathVariable Long id, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        try {
            CaseResponse caseResponse = caseUseCase.getCaseDetails(id, currentUser);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", caseResponse);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            if (e.getMessage().contains("403_FORBIDDEN")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateCase(@PathVariable Long id, @RequestBody UpdateCaseRequest request, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        try {
            CaseResponse caseResponse = caseUseCase.updateCase(id, request, currentUser);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", caseResponse);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            if (e.getMessage().contains("403_FORBIDDEN")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            return ResponseEntity.badRequest().body(error);
        }
    }
}
