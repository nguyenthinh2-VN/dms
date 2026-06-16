package com.example.be.presentation.controller;

import com.example.be.application.dto.AuthResponse;
import com.example.be.application.util.MessageUtils;
import com.example.be.application.dto.LoginRequest;
import com.example.be.application.dto.RegisterRequest;
import com.example.be.application.usecase.LoginUseCase;
import com.example.be.application.usecase.RegisterUseCase;
import com.example.be.domain.entity.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RegisterUseCase registerUseCase;
    private final LoginUseCase loginUseCase;

    public AuthController(RegisterUseCase registerUseCase, LoginUseCase loginUseCase) {
        this.registerUseCase = registerUseCase;
        this.loginUseCase = loginUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        User savedUser = registerUseCase.execute(request);
        
        Map<String, Object> data = new HashMap<>();
        data.put("id", savedUser.getId());
        data.put("email", savedUser.getWorkEmail());
        data.put("personalReferralCode", savedUser.getPersonalReferralCode());

        Map<String, Object> response = new HashMap<>();
        response.put("status", 201);
        response.put("message", MessageUtils.getMessage("SUCCESS_REGISTER", "Đăng ký thành công"));
        response.put("data", data);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = loginUseCase.execute(request);

        Map<String, Object> response = new HashMap<>();
        response.put("status", 200);
        response.put("message", MessageUtils.getMessage("SUCCESS_LOGIN", "Đăng nhập thành công"));
        response.put("data", authResponse);

        return ResponseEntity.ok(response);
    }
}
