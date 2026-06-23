package com.example.be.infrastructure.persistence.repository;

import com.example.be.domain.entity.LegalCase;
import com.example.be.domain.entity.Role;
import com.example.be.domain.entity.User;
import com.example.be.domain.repository.LegalCaseRepository;
import com.example.be.infrastructure.persistence.entity.LegalCaseJpaEntity;
import com.example.be.infrastructure.persistence.entity.RoleJpaEntity;
import com.example.be.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class JpaLegalCaseRepositoryImpl implements LegalCaseRepository {

    private final SpringDataLegalCaseRepository springDataLegalCaseRepository;

    public JpaLegalCaseRepositoryImpl(SpringDataLegalCaseRepository springDataLegalCaseRepository) {
        this.springDataLegalCaseRepository = springDataLegalCaseRepository;
    }

    @Override
    public LegalCase save(LegalCase legalCase) {
        LegalCaseJpaEntity entity = toJpaEntity(legalCase);
        LegalCaseJpaEntity savedEntity = springDataLegalCaseRepository.save(entity);
        return toDomainEntity(savedEntity);
    }

    @Override
    public Optional<LegalCase> findById(Long id) {
        return springDataLegalCaseRepository.findById(id).map(this::toDomainEntity);
    }

    @Override
    public List<LegalCase> findAll() {
        return springDataLegalCaseRepository.findAll().stream()
                .map(this::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<LegalCase> findByRelatedUserId(Long userId) {
        return springDataLegalCaseRepository.findByRelatedUserId(userId).stream()
                .map(this::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public java.math.BigDecimal sumTotalCaseValue() {
        return springDataLegalCaseRepository.sumTotalCaseValue();
    }

    @Override
    public java.math.BigDecimal sumTotalCaseValueByAssignedLawyerId(Long userId) {
        return springDataLegalCaseRepository.sumTotalCaseValueByAssignedLawyerId(userId);
    }

    @Override
    public Long countByAssignedLawyerId(Long userId) {
        return springDataLegalCaseRepository.countByAssignedLawyerId(userId);
    }

    @Override
    public long count() {
        return springDataLegalCaseRepository.count();
    }

    private LegalCaseJpaEntity toJpaEntity(LegalCase domain) {
        if (domain == null) return null;
        
        return LegalCaseJpaEntity.builder()
                .id(domain.getId())
                .generatedTitle(domain.getGeneratedTitle())
                .title(domain.getTitle())
                .category(domain.getCategory())
                .customCategory(domain.getCustomCategory())
                .description(domain.getDescription())
                .referrerName(domain.getReferrerName())
                .assignedLawyer(toUserJpaEntity(domain.getAssignedLawyer()))
                .partnerName(domain.getPartnerName())
                .internLawyerName(domain.getInternLawyerName())
                .traineeName(domain.getTraineeName())
                .caseValue(domain.getCaseValue())
                .paymentStatus(domain.getPaymentStatus())
                .status(domain.getStatus())
                .referrerPercent(domain.getReferrerPercent())
                .assignedLawyerPercent(domain.getAssignedLawyerPercent())
                .partnerPercent(domain.getPartnerPercent())
                .internPercent(domain.getInternPercent())
                .traineePercent(domain.getTraineePercent())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    private LegalCase toDomainEntity(LegalCaseJpaEntity entity) {
        if (entity == null) return null;

        return LegalCase.builder()
                .id(entity.getId())
                .generatedTitle(entity.getGeneratedTitle())
                .title(entity.getTitle())
                .category(entity.getCategory())
                .customCategory(entity.getCustomCategory())
                .description(entity.getDescription())
                .referrerName(entity.getReferrerName())
                .assignedLawyer(toUserDomainEntity(entity.getAssignedLawyer()))
                .partnerName(entity.getPartnerName())
                .internLawyerName(entity.getInternLawyerName())
                .traineeName(entity.getTraineeName())
                .caseValue(entity.getCaseValue())
                .paymentStatus(entity.getPaymentStatus())
                .status(entity.getStatus())
                .referrerPercent(entity.getReferrerPercent())
                .assignedLawyerPercent(entity.getAssignedLawyerPercent())
                .partnerPercent(entity.getPartnerPercent())
                .internPercent(entity.getInternPercent())
                .traineePercent(entity.getTraineePercent())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private UserJpaEntity toUserJpaEntity(User user) {
        if (user == null) return null;
        RoleJpaEntity roleJpa = null;
        if (user.getRole() != null) {
            roleJpa = RoleJpaEntity.builder()
                    .id(user.getRole().getId())
                    .code(user.getRole().getCode())
                    .name(user.getRole().getName())
                    .build();
        }
        return UserJpaEntity.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .workEmail(user.getWorkEmail())
                .role(roleJpa)
                .build();
    }

    private User toUserDomainEntity(UserJpaEntity entity) {
        if (entity == null) return null;
        Role role = null;
        if (entity.getRole() != null) {
            role = Role.builder()
                    .id(entity.getRole().getId())
                    .code(entity.getRole().getCode())
                    .name(entity.getRole().getName())
                    .build();
        }
        return User.builder()
                .id(entity.getId())
                .fullName(entity.getFullName())
                .workEmail(entity.getWorkEmail())
                .role(role)
                .build();
    }
}
