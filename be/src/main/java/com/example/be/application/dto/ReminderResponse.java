package com.example.be.application.dto;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class ReminderResponse {
    private Long id;
    private Long legalCaseId;
    private String legalCaseTitle;
    private OffsetDateTime deadline;
    private String note;
    private boolean isCompleted;
    private OffsetDateTime createdAt;
}
