package com.example.be.domain.repository;

import com.example.be.domain.entity.Permission;

import java.util.Optional;

public interface PermissionRepository {
    Optional<Permission> findByCode(String code);
    Permission save(Permission permission);
}
