package com.example.be.application.dto.contract;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

@Data
public class CreateContractRequest {
    @NotNull(message = "templateId không được để trống")
    private Long templateId;
    
    private Long legalCaseId;
    
    @NotNull(message = "data không được để trống")
    private Map<String, String> data;
}
