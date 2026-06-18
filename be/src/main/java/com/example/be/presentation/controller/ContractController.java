package com.example.be.presentation.controller;

import com.example.be.application.dto.contract.*;
import com.example.be.application.usecase.ContractUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/contracts")
public class ContractController {

    private final ContractUseCase contractUseCase;

    public ContractController(ContractUseCase contractUseCase) {
        this.contractUseCase = contractUseCase;
    }

    @PostMapping("/preview")
    public ResponseEntity<?> preview(@Valid @RequestBody PreviewContractRequest request,
                                     Authentication authentication) throws Exception {
        // Assume CustomUserDetails has getId()
        // Here we just use a dummy ID or cast to user details
        Long userId = extractUserId(authentication);
        ContractUseCase.MapResponse response = contractUseCase.preview(request, userId);
        return ResponseEntity.ok(new Envelope<>(200, "Thành công", response));
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreateContractRequest request,
                                    Authentication authentication) throws Exception {
        Long userId = extractUserId(authentication);
        String creatorName = extractUserName(authentication);
        ContractResponse response = contractUseCase.create(request, userId, creatorName);
        return ResponseEntity.status(201).body(new Envelope<>(201, "Thành công", response));
    }

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(required = false) Long legalCaseId,
                                  @RequestParam(required = false) Long templateId,
                                  Authentication authentication) {
        Long userId = extractUserId(authentication);
        List<ContractListItemResponse> list = contractUseCase.list(legalCaseId, templateId, userId);
        return ResponseEntity.ok(new Envelope<>(200, "Thành công", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id, Authentication authentication) throws Exception {
        Long userId = extractUserId(authentication);
        ContractResponse response = contractUseCase.getById(id, userId);
        return ResponseEntity.ok(new Envelope<>(200, "Thành công", response));
    }

    @GetMapping("/{id}/download/docx")
    public ResponseEntity<byte[]> downloadDocx(@PathVariable Long id, Authentication authentication) throws Exception {
        Long userId = extractUserId(authentication);
        byte[] docxBytes = contractUseCase.loadDocxBytes(id, userId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"contract_" + id + ".docx\"");
        
        return ResponseEntity.ok().headers(headers).body(docxBytes);
    }

    @GetMapping("/{id}/download/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id, Authentication authentication) throws Exception {
        Long userId = extractUserId(authentication);
        byte[] pdfBytes = contractUseCase.loadPdfBytes(id, userId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"contract_" + id + ".pdf\"");
        
        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    private Long extractUserId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof com.example.be.domain.entity.User) {
            return ((com.example.be.domain.entity.User) authentication.getPrincipal()).getId();
        }
        return 1L; // Fallback for testing
    }

    private String extractUserName(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof com.example.be.domain.entity.User) {
            return ((com.example.be.domain.entity.User) authentication.getPrincipal()).getFullName();
        }
        return "Unknown";
    }

    static class Envelope<T> {
        public int status;
        public String message;
        public T data;
        
        public Envelope(int status, String message, T data) {
            this.status = status;
            this.message = message;
            this.data = data;
        }
    }
}
