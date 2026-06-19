package com.example.be.infrastructure.config;

import com.example.be.infrastructure.persistence.entity.PermissionJpaEntity;
import com.example.be.infrastructure.persistence.entity.RoleJpaEntity;
import com.example.be.infrastructure.persistence.repository.SpringDataPermissionRepository;
import com.example.be.infrastructure.persistence.repository.SpringDataRoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final SpringDataRoleRepository roleRepository;
    private final SpringDataPermissionRepository permissionRepository;
    private final com.example.be.infrastructure.persistence.repository.SpringDataUserRepository userRepository;
    private final com.example.be.infrastructure.persistence.repository.SpringDataRuleRepository ruleRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public DataInitializer(SpringDataRoleRepository roleRepository, 
                           SpringDataPermissionRepository permissionRepository,
                           com.example.be.infrastructure.persistence.repository.SpringDataUserRepository userRepository,
                           com.example.be.infrastructure.persistence.repository.SpringDataRuleRepository ruleRepository,
                           org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.userRepository = userRepository;
        this.ruleRepository = ruleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        initRoles();
        seedPermissions();
        initSuperAdmin();
    }

    private void initSuperAdmin() {
        RoleJpaEntity superAdminRole = roleRepository.findByCode("SUPER_ADMIN").orElseThrow();
        
        com.example.be.infrastructure.persistence.entity.UserJpaEntity admin = userRepository.findByWorkEmail("admin@dms.com").orElse(null);
        if (admin == null) {
            admin = com.example.be.infrastructure.persistence.entity.UserJpaEntity.builder()
                    .fullName("Super Admin")
                    .workEmail("admin@dms.com")
                    .phoneNumber("0000000000")
                    .position("Administrator")
                    .password(passwordEncoder.encode("123456"))
                    .status("ACTIVE")
                    .role(superAdminRole)
                    .personalReferralCode("ADMIN-REF")
                    .build();
            
            admin = userRepository.save(admin);
            System.out.println("Inserted default SUPER_ADMIN user: admin@dms.com");
        }

        List<com.example.be.infrastructure.persistence.entity.RuleJpaEntity> existingRules = ruleRepository.findByUserId(admin.getId());
        List<PermissionJpaEntity> allPermissions = permissionRepository.findAll();
        for (PermissionJpaEntity permission : allPermissions) {
            boolean hasRule = existingRules.stream()
                    .anyMatch(r -> r.getPermissionId().equals(permission.getId()));
            if (!hasRule) {
                com.example.be.infrastructure.persistence.entity.RuleJpaEntity rule = com.example.be.infrastructure.persistence.entity.RuleJpaEntity.builder()
                        .userId(admin.getId())
                        .permissionId(permission.getId())
                        .status("GRANTED")
                        .build();
                ruleRepository.save(rule);
            }
        }
        System.out.println("Ensured all permissions granted to SUPER_ADMIN.");
    }

    private void initRoles() {
        List<RoleData> defaultRoles = Arrays.asList(
                new RoleData("SUPER_ADMIN", "Super Admin", "Quản trị viên cấp cao nhất hệ thống"),
                new RoleData("ADMIN", "Admin", "Quản trị viên hệ thống"),
                new RoleData("PARTNER", "Partner", "Luật sư thành viên (Partner)"),
                new RoleData("LAWYER", "Lawyer", "Luật sư"),
                new RoleData("INTERN_LAWYER", "Intern Lawyer", "Luật sư thực tập"),
                new RoleData("TRAINEE", "Trainee", "Thực tập sinh")
        );

        for (RoleData data : defaultRoles) {
            if (roleRepository.findByCode(data.code).isEmpty()) {
                RoleJpaEntity role = RoleJpaEntity.builder()
                        .code(data.code)
                        .name(data.name)
                        .description(data.description)
                        .status("ACTIVE")
                        .build();
                roleRepository.save(role);
                System.out.println("Inserted default role: " + data.code);
            }
        }
    }

    private void seedPermissions() {
        List<PermissionData> permissions = Arrays.asList(
                new PermissionData("contract_template.create", "Upload file Word + lưu mẫu hợp đồng"),
                new PermissionData("contract_template.view", "Xem chi tiết mẫu (gồm field schema)"),
                new PermissionData("contract_template.list", "Xem danh sách mẫu"),
                new PermissionData("contract_template.update", "Sửa mẫu (tạo version mới)"),
                new PermissionData("contract_template.archive", "Chuyển mẫu sang ARCHIVED"),
                new PermissionData("contract.create", "Tạo hợp đồng từ mẫu"),
                new PermissionData("contract.view", "Xem chi tiết hợp đồng"),
                new PermissionData("contract.list", "Xem danh sách hợp đồng (cá nhân/vụ việc)"),
                new PermissionData("contract.list.all", "Xem toàn bộ danh sách hợp đồng hệ thống"),
                new PermissionData("contract.update", "Cập nhật hợp đồng"),
                new PermissionData("contract.delete", "Xóa hợp đồng"),
                new PermissionData("user.create", "Tạo tài khoản mới"),
                new PermissionData("user.update", "Cập nhật tài khoản"),
                new PermissionData("user.view", "Xem chi tiết tài khoản"),
                new PermissionData("user.list", "Xem danh sách tài khoản"),
                new PermissionData("user.update_status", "Khóa / mở khóa tài khoản"),
                new PermissionData("user.delete", "Xóa tài khoản")
        );

        for (PermissionData data : permissions) {
            if (permissionRepository.findByCode(data.code).isEmpty()) {
                PermissionJpaEntity permission = PermissionJpaEntity.builder()
                        .code(data.code)
                        .description(data.description)
                        .build();
                permissionRepository.save(permission);
                System.out.println("Inserted permission: " + data.code);
            }
        }
    }

    private static class RoleData {
        String code;
        String name;
        String description;

        RoleData(String code, String name, String description) {
            this.code = code;
            this.name = name;
            this.description = description;
        }
    }

    private static class PermissionData {
        String code;
        String description;

        PermissionData(String code, String description) {
            this.code = code;
            this.description = description;
        }
    }
}
