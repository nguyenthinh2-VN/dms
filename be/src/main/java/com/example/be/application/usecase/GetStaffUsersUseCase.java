package com.example.be.application.usecase;

import com.example.be.application.dto.StaffUserDto;
import com.example.be.domain.entity.User;
import com.example.be.domain.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GetStaffUsersUseCase {

    private final UserRepository userRepository;

    public GetStaffUsersUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Map<String, List<StaffUserDto>> execute() {
        Set<String> targetRoles = Set.of("LAWYER", "PARTNER", "INTERN_LAWYER", "TRAINEE");

        List<User> allUsers = userRepository.findAll();

        return allUsers.stream()
                .filter(u -> u.getRole() != null && targetRoles.contains(u.getRole().getCode()))
                .collect(Collectors.groupingBy(
                        u -> u.getRole().getCode(),
                        Collectors.mapping(u -> new StaffUserDto(u.getId(), u.getFullName()), Collectors.toList())
                ));
    }
}
