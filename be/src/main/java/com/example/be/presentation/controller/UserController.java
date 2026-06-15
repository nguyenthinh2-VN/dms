package com.example.be.presentation.controller;

import com.example.be.application.dto.ReferralDataResponse;
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

    public UserController(GetReferralsUseCase getReferralsUseCase, GetStaffUsersUseCase getStaffUsersUseCase) {
        this.getReferralsUseCase = getReferralsUseCase;
        this.getStaffUsersUseCase = getStaffUsersUseCase;
    }

    @GetMapping("/me/referrals")
    public ResponseEntity<Map<String, Object>> getMyReferrals(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        
        ReferralDataResponse referralData = getReferralsUseCase.execute(currentUser);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", referralData);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/staff")
    public ResponseEntity<Map<String, Object>> getStaff() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", getStaffUsersUseCase.execute());
        return ResponseEntity.ok(response);
    }
}
