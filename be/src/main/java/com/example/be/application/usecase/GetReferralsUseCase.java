package com.example.be.application.usecase;

import com.example.be.application.dto.ReferralDataResponse;
import com.example.be.application.dto.ReferredUserDto;
import com.example.be.domain.entity.User;
import com.example.be.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GetReferralsUseCase {

    private final UserRepository userRepository;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    public GetReferralsUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public ReferralDataResponse execute(User currentUser) {
        String myCode = currentUser.getPersonalReferralCode();
        
        List<User> referredUsers = userRepository.findByInvitedByCode(myCode);
        
        List<ReferredUserDto> dtos = referredUsers.stream()
                .map(u -> ReferredUserDto.builder()
                        .id(u.getId())
                        .fullName(u.getFullName())
                        .email(u.getWorkEmail())
                        .createdAt(u.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        String referralLink = frontendUrl + "/register?ref=" + myCode;

        return ReferralDataResponse.builder()
                .myReferralCode(myCode)
                .referralLink(referralLink)
                .totalReferred(dtos.size())
                .referredUsers(dtos)
                .build();
    }
}
