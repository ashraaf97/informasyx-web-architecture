package com.example.demo.service;

import com.example.demo.domain.dto.AuthResponse;

public interface PhoneVerificationService {
    
    /**
     * Sends a verification code to the provided phone number via WhatsApp
     * 
     * @param phoneNumber The phone number to send the code to
     * @return AuthResponse indicating success or failure
     */
    AuthResponse sendVerificationCode(String phoneNumber);
    
    /**
     * Verifies the provided code against the stored code for the phone number
     * 
     * @param phoneNumber The phone number associated with the code
     * @param code The verification code to verify
     * @return true if the code is valid and not expired, false otherwise
     */
    boolean verifyCode(String phoneNumber, String code);
    
    /**
     * Removes the verification code for the given phone number
     * 
     * @param phoneNumber The phone number to clear the code for
     */
    void clearCode(String phoneNumber);
}
