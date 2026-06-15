package com.example.be.infrastructure.persistence.repository;

import com.example.be.domain.entity.Rule;
import com.example.be.domain.repository.RuleRepository;
import com.example.be.infrastructure.persistence.entity.RuleJpaEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class JpaRuleRepositoryImpl implements RuleRepository {

    private final SpringDataRuleRepository springDataRuleRepository;

    public JpaRuleRepositoryImpl(SpringDataRuleRepository springDataRuleRepository) {
        this.springDataRuleRepository = springDataRuleRepository;
    }

    @Override
    public Rule save(Rule rule) {
        RuleJpaEntity entity = RuleJpaEntity.builder()
                .id(rule.getId())
                .userId(rule.getUserId())
                .permissionId(rule.getPermissionId())
                .status(rule.getStatus() != null ? rule.getStatus() : "GRANTED")
                .build();
        return toDomainEntity(springDataRuleRepository.save(entity));
    }

    @Override
    public List<Rule> findByUserId(Long userId) {
        return springDataRuleRepository.findByUserId(userId).stream()
                .map(this::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByUserIdAndPermissionId(Long userId, Long permissionId) {
        springDataRuleRepository.deleteByUserIdAndPermissionId(userId, permissionId);
    }

    private Rule toDomainEntity(RuleJpaEntity jpaEntity) {
        return Rule.builder()
                .id(jpaEntity.getId())
                .userId(jpaEntity.getUserId())
                .permissionId(jpaEntity.getPermissionId())
                .status(jpaEntity.getStatus())
                .createdAt(jpaEntity.getCreatedAt())
                .updatedAt(jpaEntity.getUpdatedAt())
                .build();
    }
}
