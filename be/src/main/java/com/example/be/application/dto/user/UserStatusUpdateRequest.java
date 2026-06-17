package com.example.be.application.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserStatusUpdateRequest {
    @NotBlank(message = "Status không được để trống")
    private String status;
}
