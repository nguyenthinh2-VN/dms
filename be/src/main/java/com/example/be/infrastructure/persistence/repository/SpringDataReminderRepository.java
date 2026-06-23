package com.example.be.infrastructure.persistence.repository;

import com.example.be.infrastructure.persistence.entity.ReminderJpaEntity;
import com.example.be.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;

public interface SpringDataReminderRepository extends JpaRepository<ReminderJpaEntity, Long> {
    List<ReminderJpaEntity> findTop10ByUserAndIsCompletedFalseAndDeadlineAfterOrderByDeadlineAsc(UserJpaEntity user, OffsetDateTime now);
}
