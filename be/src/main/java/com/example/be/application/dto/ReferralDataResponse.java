package com.example.be.application.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ReferralDataResponse {
    private String myReferralCode;
    private String referralLink;
    private int totalReferred;
    private List<ReferredUserDto> referredUsers;
}
