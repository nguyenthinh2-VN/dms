package com.example.be.application.usecase;

import com.example.be.application.dto.CaseResponse;
import com.example.be.application.dto.CreateCaseRequest;
import com.example.be.application.dto.StaffUserDto;
import com.example.be.application.dto.UpdateCaseRequest;
import com.example.be.domain.entity.CaseCategory;
import com.example.be.domain.entity.CaseStatus;
import com.example.be.domain.entity.LegalCase;
import com.example.be.domain.entity.PaymentStatus;
import com.example.be.domain.entity.User;
import com.example.be.domain.repository.LegalCaseRepository;
import com.example.be.domain.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CaseUseCase {

    private final LegalCaseRepository legalCaseRepository;
    private final UserRepository userRepository;
    private final com.example.be.domain.repository.CaseAssignmentRepository caseAssignmentRepository;

    public CaseUseCase(LegalCaseRepository legalCaseRepository, 
                       UserRepository userRepository,
                       com.example.be.domain.repository.CaseAssignmentRepository caseAssignmentRepository) {
        this.legalCaseRepository = legalCaseRepository;
        this.userRepository = userRepository;
        this.caseAssignmentRepository = caseAssignmentRepository;
    }

    public CaseResponse createCase(CreateCaseRequest request, User currentUser) {
        String roleName = currentUser.getRole() != null ? currentUser.getRole().getCode() : "";
        if (!roleName.equals("ADMIN") && !roleName.equals("SUPER_ADMIN") && !roleName.equals("LAWYER") && !roleName.equals("PARTNER")) {
            throw new RuntimeException("403_FORBIDDEN: Bạn không có quyền tạo vụ việc.");
        }

        CaseCategory categoryEnum = CaseCategory.valueOf(request.getCategory());
        String customCategory = categoryEnum == CaseCategory.OTHER ? request.getCustomCategory() : null;

        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("ddMMyy"));
        String generatedTitle = String.format("%s_%s_%s_%s", request.getTitle(), categoryEnum.getDescription(), dateStr, currentUser.getFullName());

        LegalCase newCase = LegalCase.builder()
                .generatedTitle(generatedTitle)
                .title(request.getTitle())
                .category(categoryEnum)
                .customCategory(customCategory)
                .description(request.getDescription())
                .referrerName(request.getReferrerName())
                .clientName(request.getClientName())
                .assignedLawyer(currentUser)
                .createdBy(currentUser.getId())
                .creatorName(currentUser.getFullName())
                .partnerName(request.getPartnerName())
                .internLawyerName(request.getInternLawyerName())
                .traineeName(request.getTraineeName())
                .caseValue(request.getCaseValue() != null ? request.getCaseValue() : BigDecimal.ZERO)
                .referrerPercent(request.getReferrerPercent() != null ? request.getReferrerPercent() : 0.0)
                .assignedLawyerPercent(request.getAssignedLawyerPercent() != null ? request.getAssignedLawyerPercent() : 0.0)
                .partnerPercent(request.getPartnerPercent() != null ? request.getPartnerPercent() : 0.0)
                .internPercent(request.getInternPercent() != null ? request.getInternPercent() : 0.0)
                .traineePercent(request.getTraineePercent() != null ? request.getTraineePercent() : 0.0)
                .build();

        LegalCase savedCase = legalCaseRepository.save(newCase);
        return toCaseResponse(savedCase);
    }

    public List<CaseResponse> getCases(User currentUser) {
        String roleName = currentUser.getRole() != null ? currentUser.getRole().getCode() : "";
        List<LegalCase> cases;

        if (roleName.equals("ADMIN") || roleName.equals("SUPER_ADMIN") || roleName.equals("MANAGER_LAWYER")) {
            cases = legalCaseRepository.findAll();
        } else {
            cases = legalCaseRepository.findByRelatedUserId(currentUser.getId());
        }

        return cases.stream().map(this::toCaseResponse).collect(Collectors.toList());
    }

    public CaseResponse getCaseDetails(Long id, User currentUser) {
        LegalCase legalCase = legalCaseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vụ việc."));

        if (!hasAccess(legalCase, currentUser)) {
            throw new RuntimeException("403_FORBIDDEN: Bạn không có quyền xem vụ việc này. Vụ việc chỉ được xem bởi Admin hoặc những người được phân công.");
        }

        return toCaseResponse(legalCase);
    }

    public CaseResponse updateCase(Long id, UpdateCaseRequest request, User currentUser) {
        LegalCase legalCase = legalCaseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vụ việc."));

        String roleName = currentUser.getRole() != null ? currentUser.getRole().getCode() : "";
        boolean isAdmin = roleName.equals("ADMIN") || roleName.equals("SUPER_ADMIN");
        boolean isCreator = legalCase.getAssignedLawyer() != null && legalCase.getAssignedLawyer().getId().equals(currentUser.getId());

        if (!isAdmin && !isCreator) {
            throw new RuntimeException("403_FORBIDDEN: Bạn không có quyền sửa hoặc phân công vụ việc này. Chỉ Admin hoặc người tạo mới được phép.");
        }

        legalCase.setTitle(request.getTitle());
        if (request.getCategory() != null) {
            CaseCategory newCategory = CaseCategory.valueOf(request.getCategory());
            legalCase.setCategory(newCategory);
            legalCase.setCustomCategory(newCategory == CaseCategory.OTHER ? request.getCustomCategory() : null);
        } else {
            legalCase.setCustomCategory(legalCase.getCategory() == CaseCategory.OTHER ? request.getCustomCategory() : null);
        }
        
        legalCase.setDescription(request.getDescription());
        legalCase.setReferrerName(request.getReferrerName());
        legalCase.setClientName(request.getClientName());
        legalCase.setCaseValue(request.getCaseValue() != null ? request.getCaseValue() : BigDecimal.ZERO);

        legalCase.setReferrerPercent(request.getReferrerPercent() != null ? request.getReferrerPercent() : 0.0);
        legalCase.setAssignedLawyerPercent(request.getAssignedLawyerPercent() != null ? request.getAssignedLawyerPercent() : 0.0);
        legalCase.setPartnerPercent(request.getPartnerPercent() != null ? request.getPartnerPercent() : 0.0);
        legalCase.setInternPercent(request.getInternPercent() != null ? request.getInternPercent() : 0.0);
        legalCase.setTraineePercent(request.getTraineePercent() != null ? request.getTraineePercent() : 0.0);

        if (request.getStatus() != null) {
            legalCase.setStatus(CaseStatus.valueOf(request.getStatus()));
        }
        if (request.getPaymentStatus() != null) {
            legalCase.setPaymentStatus(PaymentStatus.valueOf(request.getPaymentStatus()));
        }

        legalCase.setPartnerName(request.getPartnerName());
        legalCase.setInternLawyerName(request.getInternLawyerName());
        legalCase.setTraineeName(request.getTraineeName());

        LegalCase savedCase = legalCaseRepository.save(legalCase);
        return toCaseResponse(savedCase);
    }

    private boolean hasAccess(LegalCase legalCase, User user) {
        String roleName = user.getRole() != null ? user.getRole().getCode() : "";
        if (roleName.equals("ADMIN") || roleName.equals("SUPER_ADMIN") || roleName.equals("MANAGER_LAWYER")) return true;

        Long userId = user.getId();
        if (legalCase.getAssignedLawyer() != null && legalCase.getAssignedLawyer().getId().equals(userId)) return true;
        
        List<com.example.be.domain.entity.CaseAssignment> assignments = caseAssignmentRepository.findByCaseIdAndAssigneeId(legalCase.getId(), userId);
        if (assignments != null && !assignments.isEmpty()) {
            return true;
        }

        return false;
    }

    private CaseResponse toCaseResponse(LegalCase legalCase) {
        BigDecimal caseVal = legalCase.getCaseValue() != null ? legalCase.getCaseValue() : BigDecimal.ZERO;

        BigDecimal refVal = calculateCommission(caseVal, legalCase.getReferrerPercent());
        BigDecimal assignVal = calculateCommission(caseVal, legalCase.getAssignedLawyerPercent());
        BigDecimal partVal = calculateCommission(caseVal, legalCase.getPartnerPercent());
        BigDecimal internVal = calculateCommission(caseVal, legalCase.getInternPercent());
        BigDecimal trainVal = calculateCommission(caseVal, legalCase.getTraineePercent());

        BigDecimal totalCommissions = refVal.add(assignVal).add(partVal).add(internVal).add(trainVal);
        BigDecimal netVal = caseVal.subtract(totalCommissions);

        return CaseResponse.builder()
                .id(legalCase.getId())
                .generatedTitle(legalCase.getGeneratedTitle())
                .title(legalCase.getTitle())
                .category(legalCase.getCategory() != null ? legalCase.getCategory().name() : null)
                .customCategory(legalCase.getCustomCategory())
                .description(legalCase.getDescription())
                .referrerName(legalCase.getReferrerName())
                .clientName(legalCase.getClientName())
                .assignedLawyer(toStaffDto(legalCase.getAssignedLawyer()))
                .createdBy(legalCase.getCreatedBy())
                .creatorName(legalCase.getCreatorName())
                .partnerName(legalCase.getPartnerName())
                .internLawyerName(legalCase.getInternLawyerName())
                .traineeName(legalCase.getTraineeName())
                .caseValue(caseVal)
                .paymentStatus(legalCase.getPaymentStatus())
                .status(legalCase.getStatus())
                .referrerPercent(legalCase.getReferrerPercent())
                .assignedLawyerPercent(legalCase.getAssignedLawyerPercent())
                .partnerPercent(legalCase.getPartnerPercent())
                .internPercent(legalCase.getInternPercent())
                .traineePercent(legalCase.getTraineePercent())
                .referrerValue(refVal)
                .assignedLawyerValue(assignVal)
                .partnerValue(partVal)
                .internValue(internVal)
                .traineeValue(trainVal)
                .netValue(netVal)
                .createdAt(legalCase.getCreatedAt())
                .updatedAt(legalCase.getUpdatedAt())
                .build();
    }

    private BigDecimal calculateCommission(BigDecimal value, Double percent) {
        if (value == null || percent == null) return BigDecimal.ZERO;
        return value.multiply(BigDecimal.valueOf(percent)).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private StaffUserDto toStaffDto(User user) {
        if (user == null) return null;
        return new StaffUserDto(user.getId(), user.getFullName());
    }
}
