package com.example.be.infrastructure.persistence.repository;

import com.example.be.infrastructure.persistence.entity.LegalCaseJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataLegalCaseRepository extends JpaRepository<LegalCaseJpaEntity, Long> {

    @Query("SELECT lc FROM LegalCaseJpaEntity lc WHERE " +
           "lc.assignedLawyer.id = :userId OR " +
           "lc.partner.id = :userId OR " +
           "lc.internLawyer.id = :userId OR " +
           "lc.trainee.id = :userId")
    List<LegalCaseJpaEntity> findByRelatedUserId(@Param("userId") Long userId);
}
