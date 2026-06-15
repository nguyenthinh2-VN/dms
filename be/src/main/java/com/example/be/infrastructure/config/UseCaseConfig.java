package com.example.be.infrastructure.config;

import com.example.be.application.port.out.JwtPort;
import com.example.be.application.port.out.PasswordEncoderPort;
import com.example.be.application.usecase.LoginUseCase;
import com.example.be.application.usecase.RegisterUseCase;
import com.example.be.domain.repository.RoleRepository;
import com.example.be.domain.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public RegisterUseCase registerUseCase(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoderPort passwordEncoder) {
        return new RegisterUseCase(userRepository, roleRepository, passwordEncoder);
    }

    @Bean
    public LoginUseCase loginUseCase(UserRepository userRepository, PasswordEncoderPort passwordEncoder, JwtPort jwtPort) {
        return new LoginUseCase(userRepository, passwordEncoder, jwtPort);
    }
}
