package com.example.demo.service.impl;

import com.example.demo.domain.Person;
import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import com.example.demo.domain.dto.AuthResponse;
import com.example.demo.domain.dto.ChangePasswordRequest;
import com.example.demo.domain.dto.ForgotPasswordRequest;
import com.example.demo.domain.dto.LoginRequest;
import com.example.demo.domain.dto.PhoneLoginRequest;
import com.example.demo.domain.dto.PhoneVerificationRequest;
import com.example.demo.domain.dto.ResetPasswordRequest;
import com.example.demo.domain.dto.SignUpRequest;
import com.example.demo.domain.repository.PersonRepository;
import com.example.demo.domain.repository.UserRepository;
import com.example.demo.service.AuthService;
import com.example.demo.domain.VerificationToken;
import com.example.demo.service.EmailService;
import com.example.demo.service.PhoneVerificationService;
import com.example.demo.service.TokenService;
import com.example.demo.service.VerificationTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    // Constants
    private static final String PHONE_REGEX = "^60\\d{9,10}$";
    private static final String ERROR_OCCURRED = "Error occurred";
    private static final String PASSWORD_RESET_MESSAGE = "If an account with this email exists, you will receive password reset instructions.";

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenService verificationTokenService;
    private final EmailService emailService;
    private final PhoneVerificationService phoneVerificationService;
    private final TokenService tokenService;

    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        // Legacy login method - deprecated due to password removal
        return AuthResponse.failure("Password-based authentication is no longer supported. Please use phone number authentication.");
    }

    @Override
    public AuthResponse sendPhoneVerificationCode(PhoneLoginRequest phoneLoginRequest) {
        try {
            String phoneNumber = phoneLoginRequest.getPhoneNumber();
            
            // Validate phone number format
            if (!phoneNumber.matches(PHONE_REGEX)) {
                return AuthResponse.failure("Invalid phone number format. Must start with 60 and contain 11-12 digits total.");
            }

            // Check if user exists with this phone number
            User user = userRepository.findByPhoneNumber(phoneNumber).orElse(null);
            if (user == null) {
                return AuthResponse.failure("No account found with this phone number");
            }

            if (!user.isActive()) {
                return AuthResponse.failure("User account is deactivated");
            }

            // Send verification code
            return phoneVerificationService.sendVerificationCode(phoneNumber);

        } catch (Exception e) {
            log.error("Error sending verification code for phone number: {}", phoneLoginRequest.getPhoneNumber(), e);
            return AuthResponse.failure(ERROR_OCCURRED);
        }
    }

    @Override
    public AuthResponse verifyPhoneAndLogin(PhoneVerificationRequest phoneVerificationRequest) {
        try {
            String phoneNumber = phoneVerificationRequest.getPhoneNumber();
            String verificationCode = phoneVerificationRequest.getVerificationCode();

            // Validate phone number format
            if (!phoneNumber.matches(PHONE_REGEX)) {
                return AuthResponse.failure("Invalid phone number format");
            }

            // Find user by phone number
            User user = userRepository.findByPhoneNumber(phoneNumber).orElse(null);
            if (user == null) {
                return AuthResponse.failure(ERROR_OCCURRED);
            }

            if (!user.isActive()) {
                return AuthResponse.failure(ERROR_OCCURRED);
            }

            // Verify the code
            boolean isCodeValid = phoneVerificationService.verifyCode(phoneNumber, verificationCode);
            if (!isCodeValid) {
                return AuthResponse.failure("Invalid or expired verification code");
            }

            // Mark phone as verified if not already
            if (!user.isPhoneVerified()) {
                user.setPhoneVerified(true);
                userRepository.save(user);
            }

            // Generate and store authentication token
            String token = tokenService.generateToken(user.getUsername());
            tokenService.storeToken(token, user.getUsername());
            
            log.info("User {} with role {} logged in successfully via phone verification", user.getUsername(), user.getRole());
            return AuthResponse.success(user.getUsername(), token, user.getRole());

        } catch (Exception e) {
            log.error("Error verifying phone and login for phone number: {}", phoneVerificationRequest.getPhoneNumber(), e);
            return AuthResponse.failure(ERROR_OCCURRED);
        }
    }

    @Override
    public AuthResponse logout(String token) {
        try {
            String username = tokenService.getUsernameFromToken(token);
            if (username != null) {
                tokenService.removeToken(token);
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

    public boolean isValidToken(String token) {
        return tokenService.isValidToken(token);
    }

    public String getUsernameFromToken(String token) {
        return tokenService.getUsernameFromToken(token);
    }

    @Override
    public AuthResponse changePassword(String token, ChangePasswordRequest changePasswordRequest) {
        // Password change is no longer supported since we removed password authentication
        return AuthResponse.failure("Password change is no longer supported. Authentication is now phone-based.");
    }

    @Override
    public AuthResponse signUp(SignUpRequest signUpRequest) {
        try {
            // Validate phone number format
            if (signUpRequest.getPhoneNumber() == null || 
                !signUpRequest.getPhoneNumber().matches(PHONE_REGEX)) {
                return AuthResponse.failure("Phone number is required and must start with 60 and contain 11-12 digits total");
            }

            if (userRepository.findByUsername(signUpRequest.getUsername()).isPresent()) {
                return AuthResponse.failure("Username already exists");
            }

            if (personRepository.findByEmail(signUpRequest.getEmail()).isPresent()) {
                return AuthResponse.failure("Email already exists");
            }

            // Check if phone number already exists
            if (userRepository.existsByPhoneNumber(signUpRequest.getPhoneNumber())) {
                return AuthResponse.failure("Phone number already exists");
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
            user.setPerson(savedPerson);
            user.setActive(true);
            user.setEmailVerified(false); // Will be verified via email
            user.setPhoneVerified(false); // Will be verified when they first login
            user.setRole(Role.USER);

            User savedUser = userRepository.save(user);

            // Generate email verification token and send verification email
            String verificationToken = verificationTokenService.generateEmailVerificationToken(savedUser);
            emailService.sendEmailVerification(savedUser, verificationToken);

            log.info("New user registered: {}", signUpRequest.getUsername());
            return new AuthResponse(null, signUpRequest.getUsername(), 
                "User registered successfully! Please check your email to verify your account. Use your phone number to login.", true);

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
                    PASSWORD_RESET_MESSAGE, true);
            }

            User user = userRepository.findByUsername(person.getUser().getUsername())
                .orElse(null);
                
            if (user == null || !user.isActive()) {
                return new AuthResponse(null, null, 
                    PASSWORD_RESET_MESSAGE, true);
            }

            // Generate password reset token and send email
            String resetToken = verificationTokenService.generatePasswordResetToken(user);
            emailService.sendPasswordResetEmail(user, resetToken);

            log.info("Password reset requested for user: {}", user.getUsername());
            return new AuthResponse(null, null, 
                PASSWORD_RESET_MESSAGE, true);

        } catch (Exception e) {
            log.error("Forgot password error", e);
            return AuthResponse.failure("Password reset request failed");
        }
    }

    @Override
    public AuthResponse resetPassword(ResetPasswordRequest resetPasswordRequest) {
        // Password reset is no longer supported since we removed password authentication
        return AuthResponse.failure("Password reset is no longer supported. Authentication is now phone-based. Please use your phone number to login.");
    }
}