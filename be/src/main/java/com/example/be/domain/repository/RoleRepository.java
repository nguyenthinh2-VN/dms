package com.example.be.domain.repository;

import com.example.be.domain.entity.Role;
import java.util.List;
import java.util.Optional;

public interface RoleRepository {
    Optional<Role> findByName(String name);
    Optional<Role> findByCode(String code);
    List<Role> findAll();
}
