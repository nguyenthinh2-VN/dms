package com.example.be.infrastructure.persistence.repository;

import com.example.be.domain.entity.Role;
import com.example.be.domain.repository.RoleRepository;
import com.example.be.infrastructure.persistence.entity.RoleJpaEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class JpaRoleRepositoryImpl implements RoleRepository {
    
    private final SpringDataRoleRepository springDataRoleRepository;

    public JpaRoleRepositoryImpl(SpringDataRoleRepository springDataRoleRepository) {
        this.springDataRoleRepository = springDataRoleRepository;
    }

    @Override
    public Optional<Role> findByName(String name) {
        return springDataRoleRepository.findByName(name)
                .map(this::toDomainEntity);
    }

    @Override
    public Optional<Role> findByCode(String code) {
        return springDataRoleRepository.findByCode(code)
                .map(this::toDomainEntity);
    }

    @Override
    public java.util.List<Role> findAll() {
        return springDataRoleRepository.findAll().stream()
                .map(this::toDomainEntity)
                .collect(java.util.stream.Collectors.toList());
    }

    private Role toDomainEntity(RoleJpaEntity jpaEntity) {
        return Role.builder()
                .id(jpaEntity.getId())
                .code(jpaEntity.getCode())
                .name(jpaEntity.getName())
                .description(jpaEntity.getDescription())
                .status(jpaEntity.getStatus())
                .createdAt(jpaEntity.getCreatedAt())
                .updatedAt(jpaEntity.getUpdatedAt())
                .build();
    }
}
