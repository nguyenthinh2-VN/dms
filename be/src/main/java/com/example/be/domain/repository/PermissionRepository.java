package com.example.be.domain.repository;

import com.example.be.domain.entity.Permission;

import java.util.List;
import java.util.Optional;

public interface PermissionRepository {
    Optional<Permission> findByCode(String code);
    Permission save(Permission permission);
    List<Permission> findAll();
}
