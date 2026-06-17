package com.example.be.application.usecase;

import com.example.be.application.dto.auth.RegisterRequest;
import com.example.be.application.port.out.PasswordEncoderPort;
import com.example.be.domain.entity.Role;
import com.example.be.domain.entity.User;
import com.example.be.domain.exception.InvalidReferralCodeException;
import com.example.be.domain.exception.InvalidRoleException;
import com.example.be.domain.exception.UserAlreadyExistsException;
import com.example.be.domain.repository.RoleRepository;
import com.example.be.domain.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class RegisterUseCase {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoderPort passwordEncoder;

    // Roles that are blocked from registration
    private static final List<String> BLOCKED_ROLES = List.of("ADMIN", "SUPER_ADMIN");

    public RegisterUseCase(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoderPort passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User execute(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu và xác nhận mật khẩu không khớp");
        }

        if (BLOCKED_ROLES.contains(request.getRole())) {
            throw new InvalidRoleException("Không thể đăng ký với vai trò này");
        }

        if (userRepository.existsByWorkEmail(request.getWorkEmail())) {
            throw new UserAlreadyExistsException("Email đã được sử dụng");
        }

        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new UserAlreadyExistsException("Số điện thoại đã được sử dụng");
        }

        if (request.getInvitedByCode() != null && !request.getInvitedByCode().trim().isEmpty()) {
            if (!userRepository.existsByPersonalReferralCode(request.getInvitedByCode())) {
                throw new InvalidReferralCodeException("Mã giới thiệu không tồn tại trong hệ thống");
            }
        }

        Role role = roleRepository.findByCode(request.getRole())
                .orElseThrow(() -> new InvalidRoleException("Vai trò không tồn tại"));

        String personalReferralCode = generateUniqueReferralCode();

        User newUser = User.builder()
                .fullName(request.getFullName())
                .workEmail(request.getWorkEmail())
                .position(request.getPosition())
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .invitedByCode(request.getInvitedByCode())
                .personalReferralCode(personalReferralCode)
                .role(role)
                .build();

        return userRepository.save(newUser);
    }

    private String generateUniqueReferralCode() {
        String code;
        do {
            code = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (userRepository.existsByPersonalReferralCode(code));
        return code;
    }
}
