package com.example.be.infrastructure.persistence.repository;

import com.example.be.domain.entity.CaseAssignment;
import com.example.be.domain.entity.LegalCase;
import com.example.be.domain.entity.User;
import com.example.be.domain.repository.CaseAssignmentRepository;
import com.example.be.infrastructure.persistence.entity.CaseAssignmentJpaEntity;
import com.example.be.infrastructure.persistence.entity.LegalCaseJpaEntity;
import com.example.be.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class JpaCaseAssignmentRepositoryImpl implements CaseAssignmentRepository {

    private final SpringDataCaseAssignmentRepository springDataRepository;

    public JpaCaseAssignmentRepositoryImpl(SpringDataCaseAssignmentRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public CaseAssignment save(CaseAssignment assignment) {
        CaseAssignmentJpaEntity entity = toJpaEntity(assignment);
        CaseAssignmentJpaEntity savedEntity = springDataRepository.save(entity);
        return toDomainEntity(savedEntity);
    }

    @Override
    public List<CaseAssignment> saveAll(List<CaseAssignment> assignments) {
        List<CaseAssignmentJpaEntity> entities = assignments.stream().map(this::toJpaEntity).collect(Collectors.toList());
        List<CaseAssignmentJpaEntity> savedEntities = springDataRepository.saveAll(entities);
        return savedEntities.stream().map(this::toDomainEntity).collect(Collectors.toList());
    }

    @Override
    public List<CaseAssignment> findByCaseId(Long caseId) {
        return springDataRepository.findByLegalCase_IdOrderByCreatedAtDesc(caseId).stream()
                .map(this::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<CaseAssignment> findByCaseIdAndAssigneeId(Long caseId, Long assigneeId) {
        return springDataRepository.findByLegalCase_IdAndAssignee_Id(caseId, assigneeId).stream()
                .map(this::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(CaseAssignment assignment) {
        springDataRepository.deleteById(assignment.getId());
    }

    private CaseAssignmentJpaEntity toJpaEntity(CaseAssignment domain) {
        if (domain == null) return null;

        LegalCaseJpaEntity caseEntity = null;
        if (domain.getLegalCase() != null) {
            caseEntity = LegalCaseJpaEntity.builder().id(domain.getLegalCase().getId()).build();
        }

        UserJpaEntity assigneeEntity = null;
        if (domain.getAssignee() != null) {
            assigneeEntity = UserJpaEntity.builder()
                    .id(domain.getAssignee().getId())
                    .fullName(domain.getAssignee().getFullName())
                    .build();
        }

        UserJpaEntity assignerEntity = null;
        if (domain.getAssigner() != null) {
            assignerEntity = UserJpaEntity.builder()
                    .id(domain.getAssigner().getId())
                    .fullName(domain.getAssigner().getFullName())
                    .build();
        }

        return CaseAssignmentJpaEntity.builder()
                .id(domain.getId())
                .legalCase(caseEntity)
                .assignee(assigneeEntity)
                .assigner(assignerEntity)
                .roleInCase(domain.getRoleInCase())
                .note(domain.getNote())
                .commissionPercent(domain.getCommissionPercent())
                .createdAt(domain.getCreatedAt())
                .build();
    }

    private CaseAssignment toDomainEntity(CaseAssignmentJpaEntity entity) {
        if (entity == null) return null;

        LegalCase caseDomain = null;
        if (entity.getLegalCase() != null) {
            caseDomain = LegalCase.builder().id(entity.getLegalCase().getId()).build();
        }

        User assigneeDomain = null;
        if (entity.getAssignee() != null) {
            assigneeDomain = User.builder().id(entity.getAssignee().getId()).fullName(entity.getAssignee().getFullName()).build();
        }

        User assignerDomain = null;
        if (entity.getAssigner() != null) {
            assignerDomain = User.builder().id(entity.getAssigner().getId()).fullName(entity.getAssigner().getFullName()).build();
        }

        return CaseAssignment.builder()
                .id(entity.getId())
                .legalCase(caseDomain)
                .assignee(assigneeDomain)
                .assigner(assignerDomain)
                .roleInCase(entity.getRoleInCase())
                .note(entity.getNote())
                .commissionPercent(entity.getCommissionPercent())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
