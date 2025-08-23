package com.example.demo.service.impl;

import com.example.demo.domain.User;
import com.example.demo.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.email.provider", havingValue = "mock", matchIfMissing = true)
@Slf4j
public class MockEmailServiceImpl implements EmailService {

    @Value("${app.base-url}")
    private String baseUrl;

    @Override
    public void sendEmailVerification(User user, String token) {
        String verificationUrl = baseUrl + "/verify-email?token=" + token;
        
        log.info("=== MOCK EMAIL: Email Verification ===");
        log.info("To: {}", user.getPerson().getEmail());
        log.info("Subject: Please verify your email address");
        log.info("Hello {}, please verify your email by clicking: {}", 
            user.getPerson().getFirstName(), verificationUrl);
        log.info("=======================================");
        
        // In a real implementation, this would send an actual email
    }

    @Override
    public void sendPasswordResetEmail(User user, String token) {
        String resetUrl = baseUrl + "/reset-password?token=" + token;
        
        log.info("=== MOCK EMAIL: Password Reset ===");
        log.info("To: {}", user.getPerson().getEmail());
        log.info("Subject: Password Reset Request");
        log.info("Hello {}, reset your password by clicking: {}", 
            user.getPerson().getFirstName(), resetUrl);
        log.info("===================================");
        
        // In a real implementation, this would send an actual email
    }

    @Override
    public void sendWelcomeEmail(User user) {
        log.info("=== MOCK EMAIL: Welcome ===");
        log.info("To: {}", user.getPerson().getEmail());
        log.info("Subject: Welcome to Informasyx!");
        log.info("Hello {}, welcome to Informasyx! Your account is now active.", 
            user.getPerson().getFirstName());
        log.info("===========================");
        
        // In a real implementation, this would send an actual email
    }
}