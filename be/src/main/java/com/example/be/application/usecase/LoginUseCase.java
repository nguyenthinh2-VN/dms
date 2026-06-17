package com.example.be.application.usecase;

import com.example.be.application.dto.auth.AuthResponse;
import com.example.be.application.dto.auth.LoginRequest;
import com.example.be.application.port.out.JwtPort;
import com.example.be.application.port.out.PasswordEncoderPort;
import com.example.be.domain.entity.User;
import com.example.be.domain.repository.UserRepository;

public class LoginUseCase {
    private final UserRepository userRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final JwtPort jwtPort;

    public LoginUseCase(UserRepository userRepository, PasswordEncoderPort passwordEncoder, JwtPort jwtPort) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtPort = jwtPort;
    }

    public AuthResponse execute(LoginRequest request) {
        User user = userRepository.findByWorkEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Email hoặc mật khẩu không chính xác"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Email hoặc mật khẩu không chính xác");
        }

        String token = jwtPort.generateToken(user);
        
        return AuthResponse.builder()
                .accessToken(token)
                .expiresIn(jwtPort.getExpirationTime())
                .role(user.getRole() != null ? user.getRole().getName() : null)
                .fullName(user.getFullName())
                .email(user.getWorkEmail())
                .build();
    }
}
