package com.example.be.application.usecase;

import com.example.be.application.dto.ReminderRequest;
import com.example.be.application.dto.ReminderResponse;
import com.example.be.domain.entity.LegalCase;
import com.example.be.domain.entity.Reminder;
import com.example.be.domain.entity.User;
import com.example.be.domain.repository.LegalCaseRepository;
import com.example.be.domain.repository.ReminderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReminderUseCase {

    private final ReminderRepository reminderRepository;
    private final LegalCaseRepository legalCaseRepository;

    public ReminderUseCase(ReminderRepository reminderRepository, LegalCaseRepository legalCaseRepository) {
        this.reminderRepository = reminderRepository;
        this.legalCaseRepository = legalCaseRepository;
    }

    @Transactional
    public ReminderResponse createReminder(ReminderRequest request, User currentUser) {
        LegalCase legalCase = legalCaseRepository.findById(request.getLegalCaseId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vụ việc với ID: " + request.getLegalCaseId()));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate date = LocalDate.parse(request.getDeadline(), formatter);
        OffsetDateTime deadline = OffsetDateTime.of(date, LocalTime.MAX, ZoneOffset.ofHours(7)); // UTC+7

        Reminder reminder = Reminder.builder()
                .user(currentUser)
                .legalCase(legalCase)
                .deadline(deadline)
                .note(request.getNote())
                .build();

        Reminder saved = reminderRepository.save(reminder);
        return toResponse(saved);
    }

    @Transactional
    public void completeReminder(Long id, User currentUser) {
        Reminder reminder = reminderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhắc hạn."));

        if (!reminder.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("403_FORBIDDEN: Không có quyền truy cập.");
        }

        reminder.setCompleted(true);
        reminderRepository.save(reminder);
    }

    public List<ReminderResponse> getUpcomingReminders(User currentUser) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.ofHours(7));
        return reminderRepository.findTop10ByUserAndIsCompletedFalseAndDeadlineAfterOrderByDeadlineAsc(currentUser, now)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private ReminderResponse toResponse(Reminder reminder) {
        String title = null;
        if (reminder.getLegalCase() != null) {
            title = reminder.getLegalCase().getTitle() != null ? reminder.getLegalCase().getTitle() : reminder.getLegalCase().getGeneratedTitle();
        }
        return ReminderResponse.builder()
                .id(reminder.getId())
                .legalCaseId(reminder.getLegalCase() != null ? reminder.getLegalCase().getId() : null)
                .legalCaseTitle(title)
                .deadline(reminder.getDeadline())
                .note(reminder.getNote())
                .isCompleted(reminder.isCompleted())
                .createdAt(reminder.getCreatedAt())
                .build();
    }
}
