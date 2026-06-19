package com.example.be.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ReminderRequest {
    @NotNull(message = "Case ID không được để trống")
    private Long legalCaseId;

    @NotBlank(message = "Thời hạn không được để trống")
    @Pattern(regexp = "^\\d{2}-\\d{2}-\\d{4}$", message = "Thời hạn phải đúng định dạng dd-MM-yyyy")
    private String deadline;

    private String note;
}
