package com.example.demo.controller;

import com.example.demo.domain.dto.AuthResponse;
import com.example.demo.domain.dto.ChangePasswordRequest;
import com.example.demo.domain.dto.ForgotPasswordRequest;
import com.example.demo.domain.dto.LoginRequest;
import com.example.demo.domain.dto.PhoneLoginRequest;
import com.example.demo.domain.dto.PhoneVerificationRequest;
import com.example.demo.domain.dto.ResetPasswordRequest;
import com.example.demo.domain.dto.SignUpRequest;
import com.example.demo.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "APIs for user authentication")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    @Operation(summary = "User registration", description = "Registers a new user account")
    public ResponseEntity<AuthResponse> signUp(@Valid @RequestBody SignUpRequest signUpRequest) {
        AuthResponse response = authService.signUp(signUpRequest);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/login")
    @Operation(summary = "User login (Deprecated)", description = "Legacy login method - no longer supported")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse response = authService.login(loginRequest);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/phone/send-code")
    @Operation(summary = "Send phone verification code", description = "Sends verification code to phone number via WhatsApp")
    public ResponseEntity<AuthResponse> sendPhoneVerificationCode(@Valid @RequestBody PhoneLoginRequest phoneLoginRequest) {
        AuthResponse response = authService.sendPhoneVerificationCode(phoneLoginRequest);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/phone/verify-login")
    @Operation(summary = "Verify phone code and login", description = "Verifies phone verification code and logs in user")
    public ResponseEntity<AuthResponse> verifyPhoneAndLogin(@Valid @RequestBody PhoneVerificationRequest phoneVerificationRequest) {
        AuthResponse response = authService.verifyPhoneAndLogin(phoneVerificationRequest);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Logs out user and invalidates token")
    public ResponseEntity<AuthResponse> logout(@RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        AuthResponse response = authService.logout(token);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/change-password")
    @Operation(summary = "Change password", description = "Changes user password after verifying current password")
    public ResponseEntity<AuthResponse> changePassword(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        AuthResponse response = authService.changePassword(token, changePasswordRequest);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Verify email address", description = "Verifies user email address with token")
    public ResponseEntity<AuthResponse> verifyEmail(@RequestParam String token) {
        AuthResponse response = authService.verifyEmail(token);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot password", description = "Sends password reset email to user")
    public ResponseEntity<AuthResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        AuthResponse response = authService.forgotPassword(forgotPasswordRequest);
        // Always return success for security reasons
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Resets user password with token")
    public ResponseEntity<AuthResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
        AuthResponse response = authService.resetPassword(resetPasswordRequest);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

}