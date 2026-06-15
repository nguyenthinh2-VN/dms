package com.example.be.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Họ và tên không được để trống")
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String workEmail;

    @NotBlank(message = "Chức vụ không được để trống")
    private String position;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^0\\d{1,14}$", message = "Số điện thoại phải bắt đầu bằng 0 và dài tối đa 15 ký tự")
    private String phoneNumber;

    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;

    @NotBlank(message = "Mật khẩu xác nhận không được để trống")
    private String confirmPassword;

    private String invitedByCode;

    @NotBlank(message = "Vai trò không được để trống")
    private String role;
}
