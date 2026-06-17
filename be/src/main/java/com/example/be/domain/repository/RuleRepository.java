package com.example.be.domain.repository;

import com.example.be.domain.entity.Rule;

import java.util.List;

public interface RuleRepository {
    Rule save(Rule rule);
    List<Rule> findByUserId(Long userId);
    void deleteByUserIdAndPermissionId(Long userId, Long permissionId);
    void deleteByUserId(Long userId);
}
