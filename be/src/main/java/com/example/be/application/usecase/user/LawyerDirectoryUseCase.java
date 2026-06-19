package com.example.be.application.usecase.user;

import com.example.be.application.dto.user.LawyerDetailResponse;
import com.example.be.application.dto.user.LawyerDirectoryItemResponse;
import com.example.be.domain.entity.User;
import com.example.be.domain.exception.ForbiddenException;
import com.example.be.domain.exception.ResourceNotFoundException;
import com.example.be.domain.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class LawyerDirectoryUseCase {

    private final UserRepository userRepository;

    public LawyerDirectoryUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private void ensureAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ForbiddenException("Authentication required to access lawyer directory");
        }
    }

    public Page<LawyerDirectoryItemResponse> getLawyerDirectory(String keyword, String rankLevel, String specialty, Integer yearsOfExperience, Pageable pageable) {
        ensureAuthenticated();
        return userRepository.searchLawyers(keyword, rankLevel, specialty, yearsOfExperience, pageable)
                .map(this::toItemResponse);
    }

    public LawyerDetailResponse getLawyerDetails(Long id) {
        ensureAuthenticated();
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lawyer not found with id: " + id));
        
        // Ensure the found user is actually a lawyer (not ADMIN/SUPER_ADMIN)
        if (user.getRole() != null && ("SUPER_ADMIN".equals(user.getRole().getCode()) || "ADMIN".equals(user.getRole().getCode()))) {
            throw new ResourceNotFoundException("Lawyer not found with id: " + id);
        }

        return toDetailResponse(user);
    }

    private LawyerDirectoryItemResponse toItemResponse(User user) {
        LawyerDirectoryItemResponse response = new LawyerDirectoryItemResponse();
        response.setId(user.getId());
        response.setFullName(user.getFullName());
        response.setRankLevel(user.getRankLevel());
        response.setSpecialty(user.getSpecialty());
        response.setYearsOfExperience(user.getYearsOfExperience());
        return response;
    }

    private LawyerDetailResponse toDetailResponse(User user) {
        return LawyerDetailResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .workEmail(user.getWorkEmail())
                .position(user.getPosition())
                .phoneNumber(user.getPhoneNumber())
                .status(user.getStatus())
                .roleCode(user.getRole() != null ? user.getRole().getCode() : null)
                .roleName(user.getRole() != null ? user.getRole().getName() : null)
                .rankLevel(user.getRankLevel())
                .specialty(user.getSpecialty())
                .yearsOfExperience(user.getYearsOfExperience())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
