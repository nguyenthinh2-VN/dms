package com.example.be.infrastructure.persistence.repository;

import com.example.be.domain.entity.Permission;
import com.example.be.domain.repository.PermissionRepository;
import com.example.be.infrastructure.persistence.entity.PermissionJpaEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class JpaPermissionRepositoryImpl implements PermissionRepository {

    private final SpringDataPermissionRepository springDataPermissionRepository;

    public JpaPermissionRepositoryImpl(SpringDataPermissionRepository springDataPermissionRepository) {
        this.springDataPermissionRepository = springDataPermissionRepository;
    }

    @Override
    public Optional<Permission> findByCode(String code) {
        return springDataPermissionRepository.findByCode(code)
                .map(this::toDomainEntity);
    }

    @Override
    public Permission save(Permission permission) {
        PermissionJpaEntity entity = PermissionJpaEntity.builder()
                .id(permission.getId())
                .code(permission.getCode())
                .description(permission.getDescription())
                .build();
        return toDomainEntity(springDataPermissionRepository.save(entity));
    }

    @Override
    public java.util.List<Permission> findAll() {
        return springDataPermissionRepository.findAll().stream()
                .map(this::toDomainEntity)
                .collect(java.util.stream.Collectors.toList());
    }

    private Permission toDomainEntity(PermissionJpaEntity jpaEntity) {
        return Permission.builder()
                .id(jpaEntity.getId())
                .code(jpaEntity.getCode())
                .description(jpaEntity.getDescription())
                .createdAt(jpaEntity.getCreatedAt())
                .updatedAt(jpaEntity.getUpdatedAt())
                .build();
    }
}
