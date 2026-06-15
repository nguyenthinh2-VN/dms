package com.example.be.domain.exception;

public class InvalidReferralCodeException extends RuntimeException {
    public InvalidReferralCodeException(String message) {
        super(message);
    }
}
