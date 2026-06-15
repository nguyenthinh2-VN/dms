package com.example.be.application.port.out;

import com.example.be.domain.entity.User;

public interface JwtPort {
    String generateToken(User user);
    long getExpirationTime();
}
