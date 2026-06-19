package com.example.be.application.usecase;

import com.example.be.application.dto.CaseAssignmentRequest;
import com.example.be.application.dto.CaseAssignmentResponse;
import com.example.be.application.dto.StaffUserDto;
import com.example.be.domain.entity.CaseAssignment;
import com.example.be.domain.entity.LegalCase;
import com.example.be.domain.entity.User;
import com.example.be.domain.repository.CaseAssignmentRepository;
import com.example.be.domain.repository.LegalCaseRepository;
import com.example.be.domain.repository.UserRepository;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AssignCaseUseCase {

    private final LegalCaseRepository legalCaseRepository;
    private final CaseAssignmentRepository caseAssignmentRepository;
    private final UserRepository userRepository;

    public AssignCaseUseCase(LegalCaseRepository legalCaseRepository, CaseAssignmentRepository caseAssignmentRepository, UserRepository userRepository) {
        this.legalCaseRepository = legalCaseRepository;
        this.caseAssignmentRepository = caseAssignmentRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public List<CaseAssignmentResponse> assignUsersToCase(Long caseId, List<CaseAssignmentRequest> requests, User currentUser) {
        LegalCase legalCase = legalCaseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vụ việc."));

        String roleName = currentUser.getRole() != null ? currentUser.getRole().getCode() : "";
        boolean isAdmin = roleName.equals("ADMIN");
        boolean isPartner = roleName.equals("PARTNER");
        boolean isCreatorOrAssigned = legalCase.getAssignedLawyer() != null && legalCase.getAssignedLawyer().getId().equals(currentUser.getId());

        if (!isAdmin && !isPartner && !isCreatorOrAssigned) {
            throw new RuntimeException("403_FORBIDDEN: Bạn không có quyền phân công vụ việc này.");
        }

        List<CaseAssignment> assignments = requests.stream().map(req -> {
            User assignee = userRepository.findById(req.getAssigneeId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân sự với ID: " + req.getAssigneeId()));

            // Update LegalCase
            switch (req.getRoleInCase()) {
                case "LAWYER":
                    legalCase.setAssignedLawyer(assignee);
                    if (req.getCommissionPercent() != null) {
                        legalCase.setAssignedLawyerPercent(req.getCommissionPercent());
                    }
                    break;
                case "PARTNER":
                    legalCase.setPartner(assignee);
                    if (req.getCommissionPercent() != null) {
                        legalCase.setPartnerPercent(req.getCommissionPercent());
                    }
                    break;
                case "INTERN_LAWYER":
                    legalCase.setInternLawyer(assignee);
                    if (req.getCommissionPercent() != null) {
                        legalCase.setInternPercent(req.getCommissionPercent());
                    }
                    break;
                case "TRAINEE":
                    legalCase.setTrainee(assignee);
                    if (req.getCommissionPercent() != null) {
                        legalCase.setTraineePercent(req.getCommissionPercent());
                    }
                    break;
                default:
                    throw new RuntimeException("Vai trò không hợp lệ: " + req.getRoleInCase());
            }

            return CaseAssignment.builder()
                    .legalCase(legalCase)
                    .assignee(assignee)
                    .assigner(currentUser)
                    .roleInCase(req.getRoleInCase())
                    .note(req.getNote())
                    .commissionPercent(req.getCommissionPercent())
                    .build();

        }).collect(Collectors.toList());

        // Save LegalCase
        legalCaseRepository.save(legalCase);

        // Save Assignments
        List<CaseAssignment> savedAssignments = caseAssignmentRepository.saveAll(assignments);

        return savedAssignments.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<CaseAssignmentResponse> getAssignments(Long caseId, User currentUser) {
        // We could add permission checks here if needed
        return caseAssignmentRepository.findByCaseId(caseId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private CaseAssignmentResponse toResponse(CaseAssignment assignment) {
        return CaseAssignmentResponse.builder()
                .id(assignment.getId())
                .legalCaseId(assignment.getLegalCase() != null ? assignment.getLegalCase().getId() : null)
                .assignee(toStaffDto(assignment.getAssignee()))
                .assigner(toStaffDto(assignment.getAssigner()))
                .roleInCase(assignment.getRoleInCase())
                .note(assignment.getNote())
                .commissionPercent(assignment.getCommissionPercent())
                .createdAt(assignment.getCreatedAt())
                .build();
    }

    private StaffUserDto toStaffDto(User user) {
        if (user == null) return null;
        return new StaffUserDto(user.getId(), user.getFullName());
    }
}
