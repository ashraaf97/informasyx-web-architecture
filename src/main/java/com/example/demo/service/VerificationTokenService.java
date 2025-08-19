package com.example.demo.service;

import com.example.demo.domain.User;
import com.example.demo.domain.VerificationToken;

public interface VerificationTokenService {
    String generateEmailVerificationToken(User user);
    String generatePasswordResetToken(User user);
    VerificationToken findByToken(String token);
    boolean verifyEmailToken(String token);
    boolean verifyPasswordResetToken(String token);
    void markTokenAsUsed(VerificationToken token);
    void cleanupExpiredTokens();
}