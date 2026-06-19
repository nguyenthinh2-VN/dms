package com.example.be.application.usecase.user;

import com.example.be.application.dto.user.*;
import com.example.be.application.port.out.PasswordEncoderPort;
import com.example.be.application.service.PermissionChecker;
import com.example.be.domain.entity.Permission;
import com.example.be.domain.entity.Role;
import com.example.be.domain.entity.Rule;
import com.example.be.domain.entity.User;
import com.example.be.domain.exception.ResourceNotFoundException;
import com.example.be.domain.exception.UserAlreadyExistsException;
import com.example.be.domain.exception.ForbiddenException;
import com.example.be.domain.repository.PermissionRepository;
import com.example.be.domain.repository.RoleRepository;
import com.example.be.domain.repository.RuleRepository;
import com.example.be.domain.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserUseCase {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RuleRepository ruleRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final PermissionChecker permissionChecker;

    public UserUseCase(UserRepository userRepository, RoleRepository roleRepository,
                       PermissionRepository permissionRepository, RuleRepository ruleRepository,
                       PasswordEncoderPort passwordEncoder, PermissionChecker permissionChecker) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.ruleRepository = ruleRepository;
        this.passwordEncoder = passwordEncoder;
        this.permissionChecker = permissionChecker;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }
        throw new ForbiddenException("User not authenticated");
    }

    public Page<UserResponse> getUsers(Pageable pageable) {
        permissionChecker.requirePermission(getCurrentUser(), "user.list");
        return userRepository.findAll(pageable).map(this::mapToResponse);
    }

    public UserResponse getUser(Long id) {
        permissionChecker.requirePermission(getCurrentUser(), "user.view");
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapToResponse(user);
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        permissionChecker.requirePermission(getCurrentUser(), "user.create");
        if (userRepository.existsByWorkEmail(request.getWorkEmail())) {
            throw new UserAlreadyExistsException("Email already exists");
        }
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new UserAlreadyExistsException("Phone number already exists");
        }

        Role role = roleRepository.findByCode(request.getRoleCode())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + request.getRoleCode()));

        String rawPassword = (request.getPassword() != null && !request.getPassword().trim().isEmpty()) ? request.getPassword() : "123456";

        User user = User.builder()
                .fullName(request.getFullName())
                .workEmail(request.getWorkEmail())
                .phoneNumber(request.getPhoneNumber())
                .position(request.getPosition())
                .password(passwordEncoder.encode(rawPassword))
                .role(role)
                .status("ACTIVE")
                .personalReferralCode("REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .build();

        return mapToResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        permissionChecker.requirePermission(getCurrentUser(), "user.update");
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getPhoneNumber() != null) {
            if (!user.getPhoneNumber().equals(request.getPhoneNumber()) && userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                throw new UserAlreadyExistsException("Phone number already exists");
            }
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getPosition() != null) user.setPosition(request.getPosition());
        
        if (request.getRoleCode() != null) {
            Role role = roleRepository.findByCode(request.getRoleCode())
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + request.getRoleCode()));
            user.setRole(role);
        }

        return mapToResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateStatus(Long id, UserStatusUpdateRequest request) {
        permissionChecker.requirePermission(getCurrentUser(), "user.update_status");
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setStatus(request.getStatus().toUpperCase());
        return mapToResponse(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id) {
        permissionChecker.requirePermission(getCurrentUser(), "user.delete");
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        ruleRepository.deleteByUserId(id);
        userRepository.deleteById(id);
    }

    @Transactional
    public UserResponse updateMyProfile(UpdateMyProfileRequest request) {
        User currentUser = getCurrentUser();
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUser.getId()));

        if (request.getFullName() != null && !request.getFullName().isBlank()) user.setFullName(request.getFullName());
        if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());
        if (request.getPassword() != null && !request.getPassword().isBlank()) user.setPassword(passwordEncoder.encode(request.getPassword()));

        if (request.getRankLevel() != null) user.setRankLevel(request.getRankLevel());
        if (request.getSpecialty() != null) user.setSpecialty(request.getSpecialty());
        if (request.getYearsOfExperience() != null) user.setYearsOfExperience(request.getYearsOfExperience());

        return mapToResponse(userRepository.save(user));
    }

    public List<RoleResponse> getRoles() {
        permissionChecker.requirePermission(getCurrentUser(), "user.list");
        return roleRepository.findAll().stream()
                .map(r -> RoleResponse.builder()
                        .code(r.getCode())
                        .name(r.getName())
                        .description(r.getDescription())
                        .build())
                .collect(Collectors.toList());
    }

    public List<PermissionResponse> getPermissions() {
        permissionChecker.requirePermission(getCurrentUser(), "user.list");
        return permissionRepository.findAll().stream()
                .map(p -> PermissionResponse.builder()
                        .code(p.getCode())
                        .description(p.getDescription())
                        .isGranted(false)
                        .build())
                .collect(Collectors.toList());
    }

    public List<PermissionResponse> getUserPermissions(Long userId) {
        permissionChecker.requirePermission(getCurrentUser(), "user.view");
        List<Permission> allPermissions = permissionRepository.findAll();
        List<Rule> userRules = ruleRepository.findByUserId(userId);
        
        List<Long> grantedPermissionIds = userRules.stream()
                .filter(r -> "GRANTED".equals(r.getStatus()))
                .map(Rule::getPermissionId)
                .toList();

        return allPermissions.stream().map(p -> PermissionResponse.builder()
                .code(p.getCode())
                .description(p.getDescription())
                .isGranted(grantedPermissionIds.contains(p.getId()))
                .build()
        ).collect(Collectors.toList());
    }

    @Transactional
    public void assignPermissions(Long userId, AssignPermissionsRequest request) {
        permissionChecker.requirePermission(getCurrentUser(), "user.update");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        ruleRepository.deleteByUserId(userId);

        List<Permission> permissions = permissionRepository.findAll();
        for (String code : request.getPermissionCodes()) {
            Permission p = permissions.stream().filter(perm -> perm.getCode().equals(code)).findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Permission not found: " + code));
            
            Rule rule = Rule.builder()
                    .userId(userId)
                    .permissionId(p.getId())
                    .status("GRANTED")
                    .build();
            ruleRepository.save(rule);
        }
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .workEmail(user.getWorkEmail())
                .position(user.getPosition())
                .phoneNumber(user.getPhoneNumber())
                .status(user.getStatus())
                .roleCode(user.getRole() != null ? user.getRole().getCode() : null)
                .roleName(user.getRole() != null ? user.getRole().getName() : null)
                .rankLevel(user.getRankLevel())
                .specialty(user.getSpecialty())
                .yearsOfExperience(user.getYearsOfExperience())
                .createdAt(user.getCreatedAt())
                .build();
    }
}