package com.example.demo.service.impl;

import com.example.demo.domain.User;
import com.example.demo.domain.VerificationToken;
import com.example.demo.domain.repository.VerificationTokenRepository;
import com.example.demo.service.VerificationTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class VerificationTokenServiceImpl implements VerificationTokenService {

    private final VerificationTokenRepository tokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public String generateEmailVerificationToken(User user) {
        // Clean up any existing email verification tokens for this user
        tokenRepository.deleteByUserAndTokenType(user, VerificationToken.TokenType.EMAIL_VERIFICATION);

        String token = generateSecureToken();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setTokenType(VerificationToken.TokenType.EMAIL_VERIFICATION);
        verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24)); // 24 hours expiry
        verificationToken.setCreatedDate(LocalDateTime.now());

        tokenRepository.save(verificationToken);
        log.info("Email verification token generated for user: {}", user.getUsername());

        return token;
    }

    @Override
    @Transactional
    public String generatePasswordResetToken(User user) {
        // Clean up any existing password reset tokens for this user
        tokenRepository.deleteByUserAndTokenType(user, VerificationToken.TokenType.PASSWORD_RESET);

        String token = generateSecureToken();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setTokenType(VerificationToken.TokenType.PASSWORD_RESET);
        verificationToken.setExpiryDate(LocalDateTime.now().plusHours(1)); // 1 hour expiry
        verificationToken.setCreatedDate(LocalDateTime.now());

        tokenRepository.save(verificationToken);
        log.info("Password reset token generated for user: {}", user.getUsername());

        return token;
    }

    @Override
    public VerificationToken findByToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }
        return tokenRepository.findByToken(token).orElse(null);
    }

    @Override
    @Transactional
    public boolean verifyEmailToken(String token) {
        VerificationToken verificationToken = findByToken(token);
        
        if (verificationToken == null) {
            log.warn("Email verification attempted with invalid token");
            return false;
        }

        if (!verificationToken.getTokenType().equals(VerificationToken.TokenType.EMAIL_VERIFICATION)) {
            log.warn("Token type mismatch for email verification");
            return false;
        }

        if (!verificationToken.isValid()) {
            log.warn("Email verification attempted with invalid/expired token for user: {}", 
                verificationToken.getUser().getUsername());
            return false;
        }

        // Mark token as used
        markTokenAsUsed(verificationToken);
        
        // Mark user as email verified
        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        
        log.info("Email verified successfully for user: {}", user.getUsername());
        return true;
    }

    @Override
    @Transactional
    public boolean verifyPasswordResetToken(String token) {
        VerificationToken verificationToken = findByToken(token);
        
        if (verificationToken == null) {
            log.warn("Password reset attempted with invalid token");
            return false;
        }

        if (!verificationToken.getTokenType().equals(VerificationToken.TokenType.PASSWORD_RESET)) {
            log.warn("Token type mismatch for password reset");
            return false;
        }

        if (!verificationToken.isValid()) {
            log.warn("Password reset attempted with invalid/expired token for user: {}", 
                verificationToken.getUser().getUsername());
            return false;
        }

        log.info("Password reset token verified for user: {}", verificationToken.getUser().getUsername());
        return true;
    }

    @Override
    @Transactional
    public void markTokenAsUsed(VerificationToken token) {
        if (token != null) {
            token.setUsed(true);
            tokenRepository.save(token);
        }
    }

    @Override
    @Transactional
    public void cleanupExpiredTokens() {
        tokenRepository.deleteByExpiryDateBeforeAndUsedTrue(LocalDateTime.now());
        log.info("Cleaned up expired tokens");
    }

    private String generateSecureToken() {
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
}