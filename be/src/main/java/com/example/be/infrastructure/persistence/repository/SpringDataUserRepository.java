package com.example.be.infrastructure.persistence.repository;

import com.example.be.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

@Repository
public interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, Long>, JpaSpecificationExecutor<UserJpaEntity> {
    Optional<UserJpaEntity> findByWorkEmail(String workEmail);
    Optional<UserJpaEntity> findByPhoneNumber(String phoneNumber);
    boolean existsByWorkEmail(String workEmail);
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByPersonalReferralCode(String personalReferralCode);
    List<UserJpaEntity> findByInvitedByCode(String invitedByCode);
}
