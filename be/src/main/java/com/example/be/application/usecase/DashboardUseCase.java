package com.example.be.application.usecase;

import com.example.be.application.dto.DashboardResponse;
import com.example.be.domain.entity.User;
import com.example.be.domain.repository.ContractRepository;
import com.example.be.domain.repository.LegalCaseRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class DashboardUseCase {

    private final LegalCaseRepository legalCaseRepository;
    private final ContractRepository contractRepository;

    public DashboardUseCase(LegalCaseRepository legalCaseRepository, ContractRepository contractRepository) {
        this.legalCaseRepository = legalCaseRepository;
        this.contractRepository = contractRepository;
    }

    public DashboardResponse getAdminStats(User currentUser) {
        String roleName = currentUser.getRole() != null ? currentUser.getRole().getCode() : "";
        if (!roleName.equals("ADMIN") && !roleName.equals("SUPER_ADMIN")) {
            throw new RuntimeException("403_FORBIDDEN: Chỉ Admin mới có quyền xem thống kê toàn hệ thống.");
        }

        Long totalCases = legalCaseRepository.count();
        
        BigDecimal totalCaseValue = legalCaseRepository.sumTotalCaseValue();
        if (totalCaseValue == null) {
            totalCaseValue = BigDecimal.ZERO;
        }

        Long totalContracts = contractRepository.count();

        return DashboardResponse.builder()
                .totalCases(totalCases)
                .totalCaseValue(totalCaseValue)
                .totalContracts(totalContracts)
                .build();
    }

    public DashboardResponse getMyStats(User currentUser) {
        Long userId = currentUser.getId();

        Long totalCases = legalCaseRepository.countByAssignedLawyerId(userId);
        if (totalCases == null) totalCases = 0L;

        BigDecimal totalCaseValue = legalCaseRepository.sumTotalCaseValueByAssignedLawyerId(userId);
        if (totalCaseValue == null) {
            totalCaseValue = BigDecimal.ZERO;
        }

        Long totalContracts = contractRepository.countByCreatedBy(userId);
        if (totalContracts == null) totalContracts = 0L;

        return DashboardResponse.builder()
                .totalCases(totalCases)
                .totalCaseValue(totalCaseValue)
                .totalContracts(totalContracts)
                .build();
    }
}
