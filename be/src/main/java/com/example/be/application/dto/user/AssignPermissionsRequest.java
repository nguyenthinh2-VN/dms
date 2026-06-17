package com.example.be.application.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class AssignPermissionsRequest {
    @NotNull(message = "Danh sách quyền không được để null")
    private List<String> permissionCodes;
}
