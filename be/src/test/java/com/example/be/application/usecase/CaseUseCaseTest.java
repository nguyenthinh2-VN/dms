package com.example.be.application.usecase;

import com.example.be.application.dto.CaseResponse;
import com.example.be.application.dto.CreateCaseRequest;
import com.example.be.application.dto.UpdateCaseRequest;
import com.example.be.domain.entity.CaseStatus;
import com.example.be.domain.entity.LegalCase;
import com.example.be.domain.entity.PaymentStatus;
import com.example.be.domain.entity.Role;
import com.example.be.domain.entity.User;
import com.example.be.domain.repository.LegalCaseRepository;
import com.example.be.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CaseUseCaseTest {

    @Mock
    private LegalCaseRepository legalCaseRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CaseUseCase caseUseCase;

    private User adminUser;
    private User lawyerUser;
    private User normalUser;

    @BeforeEach
    void setUp() {
        Role adminRole = new Role(1L, "ADMIN", "Admin", "Admin desc", "ACTIVE", null, null);
        Role lawyerRole = new Role(2L, "LAWYER", "Lawyer", "Lawyer desc", "ACTIVE", null, null);
        Role normalRole = new Role(3L, "USER", "User", "User desc", "ACTIVE", null, null);

        adminUser = User.builder().id(1L).fullName("Admin").role(adminRole).build();
        lawyerUser = User.builder().id(2L).fullName("Lawyer").role(lawyerRole).build();
        normalUser = User.builder().id(3L).fullName("Normal").role(normalRole).build();
    }

    @Test
    void createCase_ShouldThrowException_WhenRoleIsInvalid() {
        CreateCaseRequest request = new CreateCaseRequest();
        request.setTitle("Test Case");

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> caseUseCase.createCase(request, normalUser));

        assertEquals("403_FORBIDDEN: Bạn không có quyền tạo vụ việc.", exception.getMessage());
        verify(legalCaseRepository, never()).save(any());
    }

    @Test
    void createCase_ShouldSuccess_WhenRoleIsValid() {
        CreateCaseRequest request = new CreateCaseRequest();
        request.setTitle("Test Title");
        request.setCategory("CIVIL");
        request.setCaseValue(new BigDecimal("100000000")); // 100 mil
        request.setReferrerPercent(10.0);
        request.setAssignedLawyerPercent(20.0);

        when(legalCaseRepository.save(any(LegalCase.class))).thenAnswer(invocation -> {
            LegalCase saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        CaseResponse response = caseUseCase.createCase(request, lawyerUser);

        assertNotNull(response);
        assertEquals("Test Title", response.getTitle());
        assertEquals(CaseStatus.NEW, response.getStatus());
        assertEquals(PaymentStatus.UNPAID, response.getPaymentStatus());
        
        // Assert Commissions
        assertEquals(0, new BigDecimal("10000000.00").compareTo(response.getReferrerValue())); // 10% of 100M = 10M
        assertEquals(0, new BigDecimal("20000000.00").compareTo(response.getAssignedLawyerValue())); // 20M
        assertEquals(0, new BigDecimal("70000000.00").compareTo(response.getNetValue())); // 70M
    }

    @Test
    void getCaseDetails_ShouldThrow403_WhenUserHasNoAccess() {
        LegalCase legalCase = LegalCase.builder()
                .id(1L)
                .assignedLawyer(lawyerUser)
                .build();

        when(legalCaseRepository.findById(1L)).thenReturn(Optional.of(legalCase));

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> caseUseCase.getCaseDetails(1L, normalUser));

        assertTrue(exception.getMessage().contains("403_FORBIDDEN"));
    }

    @Test
    void updateCase_ShouldThrow403_WhenUserIsNotAdminOrCreator() {
        LegalCase legalCase = LegalCase.builder()
                .id(1L)
                .assignedLawyer(adminUser)
                .build();

        when(legalCaseRepository.findById(1L)).thenReturn(Optional.of(legalCase));

        UpdateCaseRequest request = new UpdateCaseRequest();

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> caseUseCase.updateCase(1L, request, lawyerUser));

        assertTrue(exception.getMessage().contains("403_FORBIDDEN"));
    }
}
