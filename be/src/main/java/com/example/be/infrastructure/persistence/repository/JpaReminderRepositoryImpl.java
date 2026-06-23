package com.example.be.infrastructure.persistence.repository;

import com.example.be.domain.entity.LegalCase;
import com.example.be.domain.entity.Reminder;
import com.example.be.domain.entity.User;
import com.example.be.domain.repository.ReminderRepository;
import com.example.be.infrastructure.persistence.entity.LegalCaseJpaEntity;
import com.example.be.infrastructure.persistence.entity.ReminderJpaEntity;
import com.example.be.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class JpaReminderRepositoryImpl implements ReminderRepository {

    private final SpringDataReminderRepository springDataRepository;

    public JpaReminderRepositoryImpl(SpringDataReminderRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public Reminder save(Reminder reminder) {
        ReminderJpaEntity entity = toJpaEntity(reminder);
        ReminderJpaEntity savedEntity = springDataRepository.save(entity);
        return toDomainEntity(savedEntity);
    }

    @Override
    public Optional<Reminder> findById(Long id) {
        return springDataRepository.findById(id).map(this::toDomainEntity);
    }

    @Override
    public List<Reminder> findTop10ByUserAndIsCompletedFalseAndDeadlineAfterOrderByDeadlineAsc(User user, OffsetDateTime now) {
        UserJpaEntity userEntity = null;
        if (user != null) {
            userEntity = UserJpaEntity.builder().id(user.getId()).build();
        }
        return springDataRepository.findTop10ByUserAndIsCompletedFalseAndDeadlineAfterOrderByDeadlineAsc(userEntity, now)
                .stream()
                .map(this::toDomainEntity)
                .collect(Collectors.toList());
    }

    private ReminderJpaEntity toJpaEntity(Reminder domain) {
        if (domain == null) return null;

        UserJpaEntity userEntity = null;
        if (domain.getUser() != null) {
            userEntity = UserJpaEntity.builder().id(domain.getUser().getId()).build();
        }

        LegalCaseJpaEntity caseEntity = null;
        if (domain.getLegalCase() != null) {
            caseEntity = LegalCaseJpaEntity.builder().id(domain.getLegalCase().getId()).build();
        }

        return ReminderJpaEntity.builder()
                .id(domain.getId())
                .user(userEntity)
                .legalCase(caseEntity)
                .deadline(domain.getDeadline())
                .note(domain.getNote())
                .isCompleted(domain.isCompleted())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    private Reminder toDomainEntity(ReminderJpaEntity entity) {
        if (entity == null) return null;

        User userDomain = null;
        if (entity.getUser() != null) {
            userDomain = User.builder().id(entity.getUser().getId()).build();
        }

        LegalCase caseDomain = null;
        if (entity.getLegalCase() != null) {
            caseDomain = LegalCase.builder()
                    .id(entity.getLegalCase().getId())
                    .title(entity.getLegalCase().getTitle())
                    .generatedTitle(entity.getLegalCase().getGeneratedTitle())
                    .build();
        }

        return Reminder.builder()
                .id(entity.getId())
                .user(userDomain)
                .legalCase(caseDomain)
                .deadline(entity.getDeadline())
                .note(entity.getNote())
                .isCompleted(entity.isCompleted())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
