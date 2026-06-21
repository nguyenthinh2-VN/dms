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
        boolean isAdmin = roleName.equals("ADMIN") || roleName.equals("SUPER_ADMIN");
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

    @Transactional
    public void removeAssignmentFromCase(Long caseId, Long assigneeId, User currentUser) {
        LegalCase legalCase = legalCaseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vụ việc."));

        String roleName = currentUser.getRole() != null ? currentUser.getRole().getCode() : "";
        boolean isAdmin = roleName.equals("ADMIN") || roleName.equals("SUPER_ADMIN");
        boolean isPartner = roleName.equals("PARTNER");
        boolean isCreatorOrAssigned = legalCase.getAssignedLawyer() != null && legalCase.getAssignedLawyer().getId().equals(currentUser.getId());

        if (!isAdmin && !isPartner && !isCreatorOrAssigned) {
            throw new RuntimeException("403_FORBIDDEN: Bạn không có quyền xóa phân công vụ việc này.");
        }

        List<CaseAssignment> assignments = caseAssignmentRepository.findByCaseIdAndAssigneeId(caseId, assigneeId);
        if (assignments.isEmpty()) {
            throw new RuntimeException("Không tìm thấy phân công cho nhân sự này.");
        }

        for (CaseAssignment assignment : assignments) {
            String roleInCase = assignment.getRoleInCase();
            caseAssignmentRepository.delete(assignment);

            // Cập nhật lại LegalCase nếu người bị xóa đang là người được gán chính trong role này
            switch (roleInCase) {
                case "LAWYER":
                    if (legalCase.getAssignedLawyer() != null && legalCase.getAssignedLawyer().getId().equals(assigneeId)) {
                        legalCase.setAssignedLawyer(null);
                        legalCase.setAssignedLawyerPercent(0.0);
                    }
                    break;
                case "PARTNER":
                    if (legalCase.getPartner() != null && legalCase.getPartner().getId().equals(assigneeId)) {
                        legalCase.setPartner(null);
                        legalCase.setPartnerPercent(0.0);
                    }
                    break;
                case "INTERN_LAWYER":
                    if (legalCase.getInternLawyer() != null && legalCase.getInternLawyer().getId().equals(assigneeId)) {
                        legalCase.setInternLawyer(null);
                        legalCase.setInternPercent(0.0);
                    }
                    break;
                case "TRAINEE":
                    if (legalCase.getTrainee() != null && legalCase.getTrainee().getId().equals(assigneeId)) {
                        legalCase.setTrainee(null);
                        legalCase.setTraineePercent(0.0);
                    }
                    break;
            }
        }

        legalCaseRepository.save(legalCase);
    }

    private StaffUserDto toStaffDto(User user) {
        if (user == null) return null;
        return new StaffUserDto(user.getId(), user.getFullName());
    }
}
