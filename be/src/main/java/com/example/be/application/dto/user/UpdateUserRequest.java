package com.example.be.application.dto.user;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {
    private String fullName;

    @Size(min = 10, max = 15, message = "Số điện thoại từ 10-15 ký tự")
    private String phoneNumber;

    private String position;
    
    private String roleCode;
}
