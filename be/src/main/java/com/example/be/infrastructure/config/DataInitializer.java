package com.example.be.infrastructure.config;

import com.example.be.infrastructure.persistence.entity.RoleJpaEntity;
import com.example.be.infrastructure.persistence.repository.SpringDataRoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final SpringDataRoleRepository roleRepository;

    public DataInitializer(SpringDataRoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) {
        initRoles();
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
}
