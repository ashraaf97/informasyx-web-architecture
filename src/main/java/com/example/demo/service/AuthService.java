package com.example.demo.service;

import com.example.demo.domain.dto.AuthResponse;
import com.example.demo.domain.dto.ChangePasswordRequest;
import com.example.demo.domain.dto.ForgotPasswordRequest;
import com.example.demo.domain.dto.LoginRequest;
import com.example.demo.domain.dto.PhoneLoginRequest;
import com.example.demo.domain.dto.PhoneVerificationRequest;
import com.example.demo.domain.dto.ResetPasswordRequest;
import com.example.demo.domain.dto.SignUpRequest;

public interface AuthService {
    // Legacy login method (kept for backward compatibility if needed)
    AuthResponse login(LoginRequest loginRequest);
    
    // New phone-based authentication methods
    AuthResponse sendPhoneVerificationCode(PhoneLoginRequest phoneLoginRequest);
    AuthResponse verifyPhoneAndLogin(PhoneVerificationRequest phoneVerificationRequest);
    
    AuthResponse logout(String token);
    AuthResponse changePassword(String token, ChangePasswordRequest changePasswordRequest);
    AuthResponse signUp(SignUpRequest signUpRequest);
    AuthResponse verifyEmail(String token);
    AuthResponse forgotPassword(ForgotPasswordRequest forgotPasswordRequest);
    AuthResponse resetPassword(ResetPasswordRequest resetPasswordRequest);
}