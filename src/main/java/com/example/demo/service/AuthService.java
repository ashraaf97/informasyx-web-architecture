package com.example.demo.service;

import com.example.demo.domain.dto.AuthResponse;
import com.example.demo.domain.dto.ChangePasswordRequest;
import com.example.demo.domain.dto.ForgotPasswordRequest;
import com.example.demo.domain.dto.LoginRequest;
import com.example.demo.domain.dto.ResetPasswordRequest;
import com.example.demo.domain.dto.SignUpRequest;

public interface AuthService {
    AuthResponse login(LoginRequest loginRequest);
    AuthResponse logout(String token);
    AuthResponse changePassword(String token, ChangePasswordRequest changePasswordRequest);
    AuthResponse signUp(SignUpRequest signUpRequest);
    AuthResponse verifyEmail(String token);
    AuthResponse forgotPassword(ForgotPasswordRequest forgotPasswordRequest);
    AuthResponse resetPassword(ResetPasswordRequest resetPasswordRequest);
}