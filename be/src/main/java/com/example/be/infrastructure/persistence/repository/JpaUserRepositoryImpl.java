package com.example.be.infrastructure.persistence.repository;

import com.example.be.domain.entity.Role;
import com.example.be.domain.entity.User;
import com.example.be.domain.repository.UserRepository;
import com.example.be.infrastructure.persistence.entity.RoleJpaEntity;
import com.example.be.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class JpaUserRepositoryImpl implements UserRepository {

    private final SpringDataUserRepository springDataUserRepository;

    public JpaUserRepositoryImpl(SpringDataUserRepository springDataUserRepository) {
        this.springDataUserRepository = springDataUserRepository;
    }

    @Override
    public User save(User user) {
        UserJpaEntity entity = toJpaEntity(user);
        UserJpaEntity savedEntity = springDataUserRepository.save(entity);
        return toDomainEntity(savedEntity);
    }

    @Override
    public Optional<User> findByWorkEmail(String email) {
        return springDataUserRepository.findByWorkEmail(email)
                .map(this::toDomainEntity);
    }

    @Override
    public Optional<User> findById(Long id) {
        return springDataUserRepository.findById(id)
                .map(this::toDomainEntity);
    }

    @Override
    public Optional<User> findByPhoneNumber(String phoneNumber) {
        return springDataUserRepository.findByPhoneNumber(phoneNumber)
                .map(this::toDomainEntity);
    }

    @Override
    public boolean existsByWorkEmail(String email) {
        return springDataUserRepository.existsByWorkEmail(email);
    }

    @Override
    public boolean existsByPhoneNumber(String phoneNumber) {
        return springDataUserRepository.existsByPhoneNumber(phoneNumber);
    }

    @Override
    public boolean existsByPersonalReferralCode(String personalReferralCode) {
        return springDataUserRepository.existsByPersonalReferralCode(personalReferralCode);
    }

    @Override
    public List<User> findByInvitedByCode(String invitedByCode) {
        return springDataUserRepository.findByInvitedByCode(invitedByCode).stream()
                .map(this::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> findAll() {
        return springDataUserRepository.findAll().stream()
                .map(this::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public org.springframework.data.domain.Page<User> findAll(org.springframework.data.domain.Pageable pageable) {
        return springDataUserRepository.findAll(pageable)
                .map(this::toDomainEntity);
    }

    private UserJpaEntity toJpaEntity(User user) {
        RoleJpaEntity roleJpa = null;
        if (user.getRole() != null) {
            roleJpa = RoleJpaEntity.builder()
                    .id(user.getRole().getId())
                    .code(user.getRole().getCode())
                    .name(user.getRole().getName())
                    .description(user.getRole().getDescription())
                    .status(user.getRole().getStatus())
                    .createdAt(user.getRole().getCreatedAt())
                    .updatedAt(user.getRole().getUpdatedAt())
                    .build();
        }

        return UserJpaEntity.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .workEmail(user.getWorkEmail())
                .position(user.getPosition())
                .phoneNumber(user.getPhoneNumber())
                .password(user.getPassword())
                .invitedByCode(user.getInvitedByCode())
                .personalReferralCode(user.getPersonalReferralCode())
                .role(roleJpa)
                .status(user.getStatus() != null ? user.getStatus() : "ACTIVE")
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private User toDomainEntity(UserJpaEntity entity) {
        Role role = null;
        if (entity.getRole() != null) {
            role = Role.builder()
                    .id(entity.getRole().getId())
                    .code(entity.getRole().getCode())
                    .name(entity.getRole().getName())
                    .description(entity.getRole().getDescription())
                    .status(entity.getRole().getStatus())
                    .createdAt(entity.getRole().getCreatedAt())
                    .updatedAt(entity.getRole().getUpdatedAt())
                    .build();
        }

        return User.builder()
                .id(entity.getId())
                .fullName(entity.getFullName())
                .workEmail(entity.getWorkEmail())
                .position(entity.getPosition())
                .phoneNumber(entity.getPhoneNumber())
                .password(entity.getPassword())
                .invitedByCode(entity.getInvitedByCode())
                .personalReferralCode(entity.getPersonalReferralCode())
                .role(role)
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
