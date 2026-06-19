package com.example.be.domain.repository;

import com.example.be.domain.entity.Reminder;
import com.example.be.domain.entity.User;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface ReminderRepository {
    Reminder save(Reminder reminder);
    Optional<Reminder> findById(Long id);
    List<Reminder> findTop3ByUserAndIsCompletedFalseAndDeadlineAfterOrderByDeadlineAsc(User user, OffsetDateTime now);
}
