package com.example.be.presentation.controller;

import com.example.be.application.dto.ReferralDataResponse;
import com.example.be.application.util.MessageUtils;
import com.example.be.application.usecase.GetReferralsUseCase;
import com.example.be.application.usecase.GetStaffUsersUseCase;
import com.example.be.domain.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final GetReferralsUseCase getReferralsUseCase;
    private final GetStaffUsersUseCase getStaffUsersUseCase;
    private final com.example.be.application.usecase.user.UserUseCase userUseCase;

    public UserController(GetReferralsUseCase getReferralsUseCase, GetStaffUsersUseCase getStaffUsersUseCase, com.example.be.application.usecase.user.UserUseCase userUseCase) {
        this.getReferralsUseCase = getReferralsUseCase;
        this.getStaffUsersUseCase = getStaffUsersUseCase;
        this.userUseCase = userUseCase;
    }

    @GetMapping("/me/referrals")
    public ResponseEntity<Map<String, Object>> getMyReferrals(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        
        ReferralDataResponse referralData = getReferralsUseCase.execute(currentUser);

        Map<String, Object> response = new HashMap<>();
        response.put("status", 200);
        response.put("message", MessageUtils.getMessage("SUCCESS", "Thành công"));
        response.put("data", referralData);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/staff")
    public ResponseEntity<Map<String, Object>> getStaff() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", 200);
        response.put("message", MessageUtils.getMessage("SUCCESS", "Thành công"));
        response.put("data", getStaffUsersUseCase.execute());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<org.springframework.data.domain.Page<com.example.be.application.dto.user.UserResponse>> getUsers(
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "20") int size,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "createdAt,desc") String[] sort) {
        
        String sortField = sort[0];
        org.springframework.data.domain.Sort.Direction sortDirection = sort.length > 1 && sort[1].equalsIgnoreCase("asc") 
            ? org.springframework.data.domain.Sort.Direction.ASC : org.springframework.data.domain.Sort.Direction.DESC;
        org.springframework.data.domain.PageRequest pageRequest = org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by(sortDirection, sortField));
        
        return ResponseEntity.ok(userUseCase.getUsers(pageRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<com.example.be.application.dto.user.UserResponse> getUser(@org.springframework.web.bind.annotation.PathVariable Long id) {
        return ResponseEntity.ok(userUseCase.getUser(id));
    }

    @org.springframework.web.bind.annotation.PostMapping
    public ResponseEntity<com.example.be.application.dto.user.UserResponse> createUser(@jakarta.validation.Valid @org.springframework.web.bind.annotation.RequestBody com.example.be.application.dto.user.CreateUserRequest request) {
        return ResponseEntity.ok(userUseCase.createUser(request));
    }

    @org.springframework.web.bind.annotation.PutMapping("/{id}")
    public ResponseEntity<com.example.be.application.dto.user.UserResponse> updateUser(
            @org.springframework.web.bind.annotation.PathVariable Long id,
            @jakarta.validation.Valid @org.springframework.web.bind.annotation.RequestBody com.example.be.application.dto.user.UpdateUserRequest request) {
        return ResponseEntity.ok(userUseCase.updateUser(id, request));
    }

    @org.springframework.web.bind.annotation.PatchMapping("/{id}/status")
    public ResponseEntity<com.example.be.application.dto.user.UserResponse> updateStatus(
            @org.springframework.web.bind.annotation.PathVariable Long id,
            @jakarta.validation.Valid @org.springframework.web.bind.annotation.RequestBody com.example.be.application.dto.user.UserStatusUpdateRequest request) {
        return ResponseEntity.ok(userUseCase.updateStatus(id, request));
    }

    @GetMapping("/roles")
    public ResponseEntity<java.util.List<com.example.be.application.dto.user.RoleResponse>> getRoles() {
        return ResponseEntity.ok(userUseCase.getRoles());
    }

    @GetMapping("/permissions")
    public ResponseEntity<java.util.List<com.example.be.application.dto.user.PermissionResponse>> getPermissions() {
        return ResponseEntity.ok(userUseCase.getPermissions());
    }

    @GetMapping("/{id}/permissions")
    public ResponseEntity<java.util.List<com.example.be.application.dto.user.PermissionResponse>> getUserPermissions(@org.springframework.web.bind.annotation.PathVariable Long id) {
        return ResponseEntity.ok(userUseCase.getUserPermissions(id));
    }

    @org.springframework.web.bind.annotation.PutMapping("/{id}/permissions")
    public ResponseEntity<Void> assignPermissions(
            @org.springframework.web.bind.annotation.PathVariable Long id,
            @jakarta.validation.Valid @org.springframework.web.bind.annotation.RequestBody com.example.be.application.dto.user.AssignPermissionsRequest request) {
        userUseCase.assignPermissions(id, request);
        return ResponseEntity.ok().build();
    }
}
