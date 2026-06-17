package com.example.be.presentation.controller;

import com.example.be.application.dto.*;
import com.example.be.application.dto.AnalyzeTemplateResponse;
import com.example.be.application.dto.ContractTemplateResponse;
import com.example.be.application.usecase.ContractTemplateUseCase;
import com.example.be.application.util.MessageUtils;
import com.example.be.domain.entity.FieldType;
import com.example.be.domain.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/contract-templates")
public class ContractTemplateController {

    private final ContractTemplateUseCase contractTemplateUseCase;

    public ContractTemplateController(ContractTemplateUseCase contractTemplateUseCase) {
        this.contractTemplateUseCase = contractTemplateUseCase;
    }

    @GetMapping("/field-types")
    public ResponseEntity<Map<String, Object>> getFieldTypes() {
        List<Map<String, String>> fieldTypes = new ArrayList<>();
        for (FieldType type : FieldType.values()) {
            Map<String, String> map = new HashMap<>();
            map.put("code", type.name());
            map.put("description", type.getDescription());
            fieldTypes.add(map);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", 200);
        response.put("message", MessageUtils.getMessage("SUCCESS", "Thành công"));
        response.put("data", fieldTypes);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/analyze")
    public ResponseEntity<Map<String, Object>> analyzeTemplate(@RequestParam("file") MultipartFile file,
                                                               Authentication authentication) throws Exception {
        User user = (User) authentication.getPrincipal();
        AnalyzeTemplateResponse data = contractTemplateUseCase.analyze(user, file);

        Map<String, Object> response = new HashMap<>();
        response.put("status", 200);
        response.put("message", MessageUtils.getMessage("SUCCESS", "Thành công"));
        response.put("data", data);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> saveTemplate(@RequestParam("file") MultipartFile file,
                                                            @RequestParam("metadata") String metadataJson,
                                                            Authentication authentication) throws Exception {
        User user = (User) authentication.getPrincipal();
        ContractTemplateResponse data = contractTemplateUseCase.save(user, file, metadataJson);

        Map<String, Object> response = new HashMap<>();
        response.put("status", 201);
        response.put("message", MessageUtils.getMessage("SUCCESS", "Thành công"));
        response.put("data", data);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> listTemplates(@RequestParam(value = "status", required = false, defaultValue = "ACTIVE") String status,
                                                             Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<ContractTemplateResponse> data = contractTemplateUseCase.listActive(user);

        Map<String, Object> response = new HashMap<>();
        response.put("status", 200);
        response.put("message", MessageUtils.getMessage("SUCCESS", "Thành công"));
        response.put("data", data);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getTemplate(@PathVariable Long id,
                                                           Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        ContractTemplateResponse data = contractTemplateUseCase.getById(user, id);

        Map<String, Object> response = new HashMap<>();
        response.put("status", 200);
        response.put("message", MessageUtils.getMessage("SUCCESS", "Thành công"));
        response.put("data", data);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateTemplate(@PathVariable Long id,
                                                              @RequestParam(value = "file", required = false) MultipartFile file,
                                                              @RequestParam(value = "metadata", required = false) String metadataJson,
                                                              Authentication authentication) throws Exception {
        User user = (User) authentication.getPrincipal();
        ContractTemplateResponse data = contractTemplateUseCase.update(user, id, file, metadataJson);

        Map<String, Object> response = new HashMap<>();
        response.put("status", 200);
        response.put("message", MessageUtils.getMessage("SUCCESS", "Thành công"));
        response.put("data", data);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/archive")
    public ResponseEntity<Map<String, Object>> archiveTemplate(@PathVariable Long id,
                                                               Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        ContractTemplateResponse data = contractTemplateUseCase.archive(user, id);

        Map<String, Object> response = new HashMap<>();
        response.put("status", 200);
        response.put("message", MessageUtils.getMessage("SUCCESS", "Thành công"));
        response.put("data", data);
        return ResponseEntity.ok(response);
    }
}
