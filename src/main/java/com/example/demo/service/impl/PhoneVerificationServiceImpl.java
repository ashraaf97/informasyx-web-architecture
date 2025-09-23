package com.example.demo.service.impl;

import com.example.demo.adapter.whatsappadapter.WhatsappAdapter;
import com.example.demo.domain.dto.AuthResponse;
import com.example.demo.service.PhoneVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class PhoneVerificationServiceImpl implements PhoneVerificationService {

    private final WhatsappAdapter whatsappAdapter;

    @Value("${whatsapp.sender.number:}")
    private String senderNumber;

    @Value("${verification.code.expiry.minutes:5}")
    private int codeExpiryMinutes;

    // Store verification codes with expiry time
    private final Map<String, VerificationCodeData> verificationCodes = new ConcurrentHashMap<>();

    // Characters for generating alphanumeric codes
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 6;
    private final SecureRandom random = new SecureRandom();

    @Override
    public AuthResponse sendVerificationCode(String phoneNumber) {
        try {
            // Generate random alphanumeric code
            String verificationCode = generateVerificationCode();
            
            // Store the code with expiry time
            VerificationCodeData codeData = new VerificationCodeData(
                verificationCode, 
                LocalDateTime.now().plusMinutes(codeExpiryMinutes)
            );
            verificationCodes.put(phoneNumber, codeData);

            // Prepare WhatsApp message
            String message = String.format("Your code is %s", verificationCode);

            log.info("Sending verification code to phone number: {} with code: {}", phoneNumber, verificationCode);
            
            // Send via WhatsApp
            whatsappAdapter.sendMessage(senderNumber, phoneNumber, message);
            
            log.info("Verification code sent successfully to phone number: {}", phoneNumber);
            return new AuthResponse(null, null, "Verification code sent successfully", true);

        } catch (Exception e) {
            log.error("Failed to send verification code to phone number: {}", phoneNumber, e);
            return AuthResponse.failure("Error occurred");
        }
    }

    @Override
    public boolean verifyCode(String phoneNumber, String code) {
        try {
            VerificationCodeData storedCodeData = verificationCodes.get(phoneNumber);
            
            if (storedCodeData == null) {
                log.warn("No verification code found for phone number: {}", phoneNumber);
                return false;
            }

            // Check if code has expired
            if (LocalDateTime.now().isAfter(storedCodeData.getExpiryTime())) {
                log.warn("Verification code expired for phone number: {}", phoneNumber);
                verificationCodes.remove(phoneNumber); // Clean up expired code
                return false;
            }

            // Check if code matches
            boolean isValid = storedCodeData.getCode().equals(code);
            
            if (isValid) {
                log.info("Verification code verified successfully for phone number: {}", phoneNumber);
                // Remove the code after successful verification
                verificationCodes.remove(phoneNumber);
            } else {
                log.warn("Invalid verification code provided for phone number: {}", phoneNumber);
            }

            return isValid;

        } catch (Exception e) {
            log.error("Error verifying code for phone number: {}", phoneNumber, e);
            return false;
        }
    }

    @Override
    public void clearCode(String phoneNumber) {
        verificationCodes.remove(phoneNumber);
        log.debug("Verification code cleared for phone number: {}", phoneNumber);
    }

    private String generateVerificationCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return code.toString();
    }

    // Inner class to store verification code with expiry time
    private static class VerificationCodeData {
        private final String code;
        private final LocalDateTime expiryTime;

        public VerificationCodeData(String code, LocalDateTime expiryTime) {
            this.code = code;
            this.expiryTime = expiryTime;
        }

        public String getCode() {
            return code;
        }

        public LocalDateTime getExpiryTime() {
            return expiryTime;
        }
    }
}
