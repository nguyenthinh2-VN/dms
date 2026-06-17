package com.example.be.domain.repository;

import com.example.be.domain.entity.User;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(Long id);
    Optional<User> findByWorkEmail(String email);
    Optional<User> findByPhoneNumber(String phoneNumber);
    boolean existsByWorkEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByPersonalReferralCode(String personalReferralCode);
    List<User> findByInvitedByCode(String invitedByCode);
    List<User> findAll();
    Page<User> findAll(Pageable pageable);
}
