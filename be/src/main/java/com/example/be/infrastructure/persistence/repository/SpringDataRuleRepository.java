package com.example.be.infrastructure.persistence.repository;

import com.example.be.infrastructure.persistence.entity.RuleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataRuleRepository extends JpaRepository<RuleJpaEntity, Long> {
    List<RuleJpaEntity> findByUserId(Long userId);
    void deleteByUserIdAndPermissionId(Long userId, Long permissionId);
}
