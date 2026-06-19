package com.example.be.domain.repository;

import com.example.be.domain.entity.CaseAssignment;
import java.util.List;

public interface CaseAssignmentRepository {
    CaseAssignment save(CaseAssignment assignment);
    List<CaseAssignment> saveAll(List<CaseAssignment> assignments);
    List<CaseAssignment> findByCaseId(Long caseId);
}
