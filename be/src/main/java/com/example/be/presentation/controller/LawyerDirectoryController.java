package com.example.be.presentation.controller;

import com.example.be.application.dto.user.LawyerDetailResponse;
import com.example.be.application.dto.user.LawyerDirectoryItemResponse;
import com.example.be.application.usecase.user.LawyerDirectoryUseCase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/directory/lawyers")
public class LawyerDirectoryController {

    private final LawyerDirectoryUseCase lawyerDirectoryUseCase;

    public LawyerDirectoryController(LawyerDirectoryUseCase lawyerDirectoryUseCase) {
        this.lawyerDirectoryUseCase = lawyerDirectoryUseCase;
    }

    @GetMapping
    public ResponseEntity<Page<LawyerDirectoryItemResponse>> getLawyerDirectory(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String rankLevel,
            @RequestParam(required = false) String specialty,
            @RequestParam(required = false) Integer yearsOfExperience,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(lawyerDirectoryUseCase.getLawyerDirectory(keyword, rankLevel, specialty, yearsOfExperience, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LawyerDetailResponse> getLawyerDetails(@PathVariable Long id) {
        return ResponseEntity.ok(lawyerDirectoryUseCase.getLawyerDetails(id));
    }
}
