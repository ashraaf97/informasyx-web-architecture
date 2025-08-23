package com.example.demo.service.impl;

import com.example.demo.domain.Person;
import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import com.example.demo.domain.dto.AuthResponse;
import com.example.demo.domain.dto.ChangePasswordRequest;
import com.example.demo.domain.dto.ForgotPasswordRequest;
import com.example.demo.domain.dto.LoginRequest;
import com.example.demo.domain.dto.ResetPasswordRequest;
import com.example.demo.domain.dto.SignUpRequest;
import com.example.demo.domain.repository.PersonRepository;
import com.example.demo.domain.repository.UserRepository;
import com.example.demo.service.AuthService;
import com.example.demo.domain.VerificationToken;
import com.example.demo.service.EmailService;
import com.example.demo.service.VerificationTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenService verificationTokenService;
    private final EmailService emailService;
    
    private final Map<String, String> tokenStore = new ConcurrentHashMap<>();

    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new BadCredentialsException("User not found"));

            if (!user.isActive()) {
                return AuthResponse.failure("User account is deactivated");
            }

            if (!user.isEmailVerified()) {
                return AuthResponse.failure("Please verify your email address before logging in");
            }

            String token = generateSimpleToken(user.getUsername());
            tokenStore.put(token, user.getUsername());
            
            log.info("User {} with role {} logged in successfully", user.getUsername(), user.getRole());
            return AuthResponse.success(user.getUsername(), token, user.getRole());

        } catch (DisabledException | LockedException e) {
            log.warn("Login attempt for deactivated user: {}", loginRequest.getUsername());
            return AuthResponse.failure("User account is deactivated");
        } catch (BadCredentialsException e) {
            log.warn("Failed login attempt for username: {}", loginRequest.getUsername());
            return AuthResponse.failure("Invalid username or password");
        } catch (Exception e) {
            log.error("Login error for username: {}", loginRequest.getUsername(), e);
            return AuthResponse.failure("Login failed");
        }
    }

    @Override
    public AuthResponse logout(String token) {
        try {
            String username = tokenStore.remove(token);
            if (username != null) {
                SecurityContextHolder.clearContext();
                log.info("User {} logged out successfully", username);
                return new AuthResponse(null, username, "Logout successful", true);
            } else {
                return AuthResponse.failure("Invalid token");
            }
        } catch (Exception e) {
            log.error("Logout error", e);
            return AuthResponse.failure("Logout failed");
        }
    }

    private String generateSimpleToken(String username) {
        return "TOKEN_" + username + "_" + System.currentTimeMillis();
    }

    public boolean isValidToken(String token) {
        return tokenStore.containsKey(token);
    }

    public String getUsernameFromToken(String token) {
        return tokenStore.get(token);
    }

    @Override
    public AuthResponse changePassword(String token, ChangePasswordRequest changePasswordRequest) {
        try {
            String username = getUsernameFromToken(token);
            if (username == null) {
                return AuthResponse.failure("Invalid or expired token");
            }

            if (!changePasswordRequest.getNewPassword().equals(changePasswordRequest.getConfirmPassword())) {
                return AuthResponse.failure("New password and confirm password do not match");
            }

            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("User not found"));

            if (!user.isActive()) {
                return AuthResponse.failure("User account is deactivated");
            }

            if (!passwordEncoder.matches(changePasswordRequest.getCurrentPassword(), user.getPassword())) {
                log.warn("Invalid current password attempt for user: {}", username);
                return AuthResponse.failure("Current password is incorrect");
            }

            String encodedNewPassword = passwordEncoder.encode(changePasswordRequest.getNewPassword());
            user.setPassword(encodedNewPassword);
            userRepository.save(user);

            log.info("Password changed successfully for user: {}", username);
            return new AuthResponse(null, username, "Password changed successfully", true);

        } catch (Exception e) {
            log.error("Change password error", e);
            return AuthResponse.failure("Failed to change password");
        }
    }

    @Override
    public AuthResponse signUp(SignUpRequest signUpRequest) {
        try {
            if (!signUpRequest.getPassword().equals(signUpRequest.getConfirmPassword())) {
                return AuthResponse.failure("Password and confirm password do not match");
            }

            if (userRepository.findByUsername(signUpRequest.getUsername()).isPresent()) {
                return AuthResponse.failure("Username already exists");
            }

            if (personRepository.findByEmail(signUpRequest.getEmail()).isPresent()) {
                return AuthResponse.failure("Email already exists");
            }

            Person person = new Person();
            person.setFirstName(signUpRequest.getFirstName());
            person.setLastName(signUpRequest.getLastName());
            person.setEmail(signUpRequest.getEmail());
            person.setPhoneNumber(signUpRequest.getPhoneNumber());
            person.setAddress(signUpRequest.getAddress());
            
            Person savedPerson = personRepository.save(person);

            User user = new User();
            user.setUsername(signUpRequest.getUsername());
            user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
            user.setPerson(savedPerson);
            user.setActive(true);
            user.setEmailVerified(false); // Will be verified via email
            user.setRole(Role.USER);

            User savedUser = userRepository.save(user);

            // Generate email verification token and send verification email
            String verificationToken = verificationTokenService.generateEmailVerificationToken(savedUser);
            emailService.sendEmailVerification(savedUser, verificationToken);

            log.info("New user registered: {}", signUpRequest.getUsername());
            return new AuthResponse(null, signUpRequest.getUsername(), 
                "User registered successfully! Please check your email to verify your account.", true);

        } catch (Exception e) {
            log.error("Sign up error for username: {}", signUpRequest.getUsername(), e);
            return AuthResponse.failure("Registration failed");
        }
    }

    @Override
    public AuthResponse verifyEmail(String token) {
        try {
            boolean verified = verificationTokenService.verifyEmailToken(token);
            if (verified) {
                VerificationToken verificationToken = verificationTokenService.findByToken(token);
                User user = verificationToken.getUser();
                
                // Send welcome email after successful verification
                emailService.sendWelcomeEmail(user);
                
                return new AuthResponse(null, user.getUsername(), 
                    "Email verified successfully! You can now log in.", true);
            } else {
                return AuthResponse.failure("Invalid or expired verification token");
            }
        } catch (Exception e) {
            log.error("Email verification error", e);
            return AuthResponse.failure("Email verification failed");
        }
    }

    @Override
    public AuthResponse forgotPassword(ForgotPasswordRequest forgotPasswordRequest) {
        try {
            Person person = personRepository.findByEmail(forgotPasswordRequest.getEmail())
                .orElse(null);
                
            if (person == null) {
                // Don't reveal if email exists or not for security
                return new AuthResponse(null, null, 
                    "If an account with this email exists, you will receive password reset instructions.", true);
            }

            User user = userRepository.findByUsername(person.getUser().getUsername())
                .orElse(null);
                
            if (user == null || !user.isActive()) {
                return new AuthResponse(null, null, 
                    "If an account with this email exists, you will receive password reset instructions.", true);
            }

            // Generate password reset token and send email
            String resetToken = verificationTokenService.generatePasswordResetToken(user);
            emailService.sendPasswordResetEmail(user, resetToken);

            log.info("Password reset requested for user: {}", user.getUsername());
            return new AuthResponse(null, null, 
                "If an account with this email exists, you will receive password reset instructions.", true);

        } catch (Exception e) {
            log.error("Forgot password error", e);
            return AuthResponse.failure("Password reset request failed");
        }
    }

    @Override
    public AuthResponse resetPassword(ResetPasswordRequest resetPasswordRequest) {
        try {
            if (!resetPasswordRequest.getNewPassword().equals(resetPasswordRequest.getConfirmPassword())) {
                return AuthResponse.failure("New password and confirm password do not match");
            }

            boolean tokenValid = verificationTokenService.verifyPasswordResetToken(resetPasswordRequest.getToken());
            if (!tokenValid) {
                return AuthResponse.failure("Invalid or expired reset token");
            }

            VerificationToken verificationToken = verificationTokenService.findByToken(resetPasswordRequest.getToken());
            User user = verificationToken.getUser();

            // Update password
            user.setPassword(passwordEncoder.encode(resetPasswordRequest.getNewPassword()));
            userRepository.save(user);

            // Mark token as used
            verificationTokenService.markTokenAsUsed(verificationToken);

            log.info("Password reset successfully for user: {}", user.getUsername());
            return new AuthResponse(null, user.getUsername(), "Password reset successful! You can now log in with your new password.", true);

        } catch (Exception e) {
            log.error("Password reset error", e);
            return AuthResponse.failure("Password reset failed");
        }
    }
}