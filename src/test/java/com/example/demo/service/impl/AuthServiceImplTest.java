package com.example.demo.service.impl;

import com.example.demo.domain.Person;
import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import com.example.demo.domain.VerificationToken;
import com.example.demo.domain.dto.*;
import com.example.demo.domain.repository.PersonRepository;
import com.example.demo.domain.repository.UserRepository;
import com.example.demo.service.EmailService;
import com.example.demo.service.VerificationTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private VerificationTokenService verificationTokenService;

    @Mock
    private EmailService emailService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private Person testPerson;
    private LoginRequest loginRequest;
    private SignUpRequest signUpRequest;
    private ChangePasswordRequest changePasswordRequest;
    private ForgotPasswordRequest forgotPasswordRequest;
    private ResetPasswordRequest resetPasswordRequest;
    private VerificationToken verificationToken;

    @BeforeEach
    void setUp() {
        testPerson = new Person();
        testPerson.setId(1L);
        testPerson.setFirstName("John");
        testPerson.setLastName("Doe");
        testPerson.setEmail("john.doe@example.com");
        testPerson.setPhoneNumber("1234567890");
        testPerson.setAddress("123 Main St");

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("johndoe");
        testUser.setPassword("encodedPassword");
        testUser.setPerson(testPerson);
        testUser.setActive(true);
        testUser.setEmailVerified(true);
        testUser.setRole(Role.USER);
        testPerson.setUser(testUser);

        loginRequest = new LoginRequest();
        loginRequest.setUsername("johndoe");
        loginRequest.setPassword("password123");

        signUpRequest = new SignUpRequest();
        signUpRequest.setUsername("newuser");
        signUpRequest.setPassword("newpass123");
        signUpRequest.setConfirmPassword("newpass123");
        signUpRequest.setFirstName("Jane");
        signUpRequest.setLastName("Smith");
        signUpRequest.setEmail("jane.smith@example.com");
        signUpRequest.setPhoneNumber("0987654321");
        signUpRequest.setAddress("456 Oak Ave");

        changePasswordRequest = new ChangePasswordRequest();
        changePasswordRequest.setCurrentPassword("oldpassword");
        changePasswordRequest.setNewPassword("newpassword123");
        changePasswordRequest.setConfirmPassword("newpassword123");

        forgotPasswordRequest = new ForgotPasswordRequest();
        forgotPasswordRequest.setEmail("john.doe@example.com");

        resetPasswordRequest = new ResetPasswordRequest();
        resetPasswordRequest.setToken("reset-token-123");
        resetPasswordRequest.setNewPassword("resetpass123");
        resetPasswordRequest.setConfirmPassword("resetpass123");

        verificationToken = new VerificationToken();
        verificationToken.setToken("verification-token-123");
        verificationToken.setUser(testUser);
        verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24));
        verificationToken.setUsed(false);
    }

    @Test
    void login_Success() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(testUser));

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("johndoe", response.getUsername());
        assertNotNull(response.getToken());
        assertTrue(response.getToken().startsWith("TOKEN_johndoe_"));
        assertEquals("Login successful", response.getMessage());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByUsername("johndoe");
    }

    @Test
    void login_UserNotActive() {
        // Arrange
        testUser.setActive(false);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(testUser));

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("User account is deactivated", response.getMessage());
        assertNull(response.getToken());
    }

    @Test
    void login_EmailNotVerified() {
        // Arrange
        testUser.setEmailVerified(false);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(testUser));

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Please verify your email address before logging in", response.getMessage());
        assertNull(response.getToken());
    }

    @Test
    void login_BadCredentials() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Invalid username or password", response.getMessage());
        assertNull(response.getToken());
    }

    @Test
    void login_DisabledException() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new DisabledException("Account disabled"));

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("User account is deactivated", response.getMessage());
        assertNull(response.getToken());
    }

    @Test
    void logout_Success() {
        // Arrange - First login to get a token
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(testUser));
        AuthResponse loginResponse = authService.login(loginRequest);
        String token = loginResponse.getToken();

        // Act
        AuthResponse response = authService.logout(token);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("johndoe", response.getUsername());
        assertEquals("Logout successful", response.getMessage());
        assertFalse(authService.isValidToken(token)); // Token should be invalidated
    }

    @Test
    void logout_InvalidToken() {
        // Act
        AuthResponse response = authService.logout("invalid-token");

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Invalid token", response.getMessage());
    }

    @Test
    void signUp_Success() {
        // Arrange
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(personRepository.findByEmail("jane.smith@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("newpass123")).thenReturn("encodedNewPass123");
        
        Person savedPerson = new Person();
        savedPerson.setId(2L);
        when(personRepository.save(any(Person.class))).thenReturn(savedPerson);
        
        User savedUser = new User();
        savedUser.setId(2L);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        
        when(verificationTokenService.generateEmailVerificationToken(any(User.class)))
                .thenReturn("verification-token");
        doNothing().when(emailService).sendEmailVerification(any(User.class), anyString());

        // Act
        AuthResponse response = authService.signUp(signUpRequest);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("newuser", response.getUsername());
        assertTrue(response.getMessage().contains("User registered successfully"));
        verify(personRepository).save(any(Person.class));
        verify(userRepository).save(any(User.class));
        verify(verificationTokenService).generateEmailVerificationToken(any(User.class));
        verify(emailService).sendEmailVerification(any(User.class), eq("verification-token"));
    }

    @Test
    void signUp_PasswordMismatch() {
        // Arrange
        signUpRequest.setConfirmPassword("differentPassword");

        // Act
        AuthResponse response = authService.signUp(signUpRequest);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Password and confirm password do not match", response.getMessage());
        verify(userRepository, never()).save(any());
        verify(personRepository, never()).save(any());
    }

    @Test
    void signUp_UsernameExists() {
        // Arrange
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.of(testUser));

        // Act
        AuthResponse response = authService.signUp(signUpRequest);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Username already exists", response.getMessage());
        verify(userRepository, never()).save(any());
        verify(personRepository, never()).save(any());
    }

    @Test
    void signUp_EmailExists() {
        // Arrange
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(personRepository.findByEmail("jane.smith@example.com")).thenReturn(Optional.of(testPerson));

        // Act
        AuthResponse response = authService.signUp(signUpRequest);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Email already exists", response.getMessage());
        verify(userRepository, never()).save(any());
        verify(personRepository, never()).save(any());
    }

    @Test
    void changePassword_Success() {
        // Arrange - Setup token first
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(testUser));
        AuthResponse loginResponse = authService.login(loginRequest);
        String token = loginResponse.getToken();

        when(passwordEncoder.matches("oldpassword", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newpassword123")).thenReturn("encodedNewPassword123");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        AuthResponse response = authService.changePassword(token, changePasswordRequest);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("johndoe", response.getUsername());
        assertEquals("Password changed successfully", response.getMessage());
        verify(passwordEncoder).matches("oldpassword", "encodedPassword");
        verify(passwordEncoder).encode("newpassword123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void changePassword_InvalidToken() {
        // Act
        AuthResponse response = authService.changePassword("invalid-token", changePasswordRequest);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Invalid or expired token", response.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_PasswordMismatch() {
        // Arrange - Setup token first
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(testUser));
        AuthResponse loginResponse = authService.login(loginRequest);
        String token = loginResponse.getToken();

        changePasswordRequest.setConfirmPassword("differentPassword");

        // Act
        AuthResponse response = authService.changePassword(token, changePasswordRequest);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("New password and confirm password do not match", response.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_WrongCurrentPassword() {
        // Arrange - Setup token first
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(testUser));
        AuthResponse loginResponse = authService.login(loginRequest);
        String token = loginResponse.getToken();

        when(passwordEncoder.matches("oldpassword", "encodedPassword")).thenReturn(false);

        // Act
        AuthResponse response = authService.changePassword(token, changePasswordRequest);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Current password is incorrect", response.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void verifyEmail_Success() {
        // Arrange
        when(verificationTokenService.verifyEmailToken("verification-token-123")).thenReturn(true);
        when(verificationTokenService.findByToken("verification-token-123")).thenReturn(verificationToken);
        doNothing().when(emailService).sendWelcomeEmail(any(User.class));

        // Act
        AuthResponse response = authService.verifyEmail("verification-token-123");

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("johndoe", response.getUsername());
        assertTrue(response.getMessage().contains("Email verified successfully"));
        verify(verificationTokenService).verifyEmailToken("verification-token-123");
        verify(emailService).sendWelcomeEmail(testUser);
    }

    @Test
    void verifyEmail_InvalidToken() {
        // Arrange
        when(verificationTokenService.verifyEmailToken("invalid-token")).thenReturn(false);

        // Act
        AuthResponse response = authService.verifyEmail("invalid-token");

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Invalid or expired verification token", response.getMessage());
        verify(emailService, never()).sendWelcomeEmail(any());
    }

    @Test
    void forgotPassword_Success() {
        // Arrange
        when(personRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(testPerson));
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(testUser));
        when(verificationTokenService.generatePasswordResetToken(testUser)).thenReturn("reset-token");
        doNothing().when(emailService).sendPasswordResetEmail(any(User.class), anyString());

        // Act
        AuthResponse response = authService.forgotPassword(forgotPasswordRequest);

        // Assert
        assertTrue(response.isSuccess());
        assertTrue(response.getMessage().contains("If an account with this email exists"));
        verify(verificationTokenService).generatePasswordResetToken(testUser);
        verify(emailService).sendPasswordResetEmail(testUser, "reset-token");
    }

    @Test
    void forgotPassword_EmailNotFound() {
        // Arrange
        when(personRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());
        forgotPasswordRequest.setEmail("nonexistent@example.com");

        // Act
        AuthResponse response = authService.forgotPassword(forgotPasswordRequest);

        // Assert
        assertTrue(response.isSuccess()); // Always returns success for security
        assertTrue(response.getMessage().contains("If an account with this email exists"));
        verify(verificationTokenService, never()).generatePasswordResetToken(any());
        verify(emailService, never()).sendPasswordResetEmail(any(), any());
    }

    @Test
    void forgotPassword_InactiveUser() {
        // Arrange
        testUser.setActive(false);
        when(personRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(testPerson));
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(testUser));

        // Act
        AuthResponse response = authService.forgotPassword(forgotPasswordRequest);

        // Assert
        assertTrue(response.isSuccess()); // Always returns success for security
        assertTrue(response.getMessage().contains("If an account with this email exists"));
        verify(verificationTokenService, never()).generatePasswordResetToken(any());
        verify(emailService, never()).sendPasswordResetEmail(any(), any());
    }

    @Test
    void resetPassword_Success() {
        // Arrange
        when(verificationTokenService.verifyPasswordResetToken("reset-token-123")).thenReturn(true);
        when(verificationTokenService.findByToken("reset-token-123")).thenReturn(verificationToken);
        when(passwordEncoder.encode("resetpass123")).thenReturn("encodedResetPass123");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        doNothing().when(verificationTokenService).markTokenAsUsed(verificationToken);

        // Act
        AuthResponse response = authService.resetPassword(resetPasswordRequest);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("johndoe", response.getUsername());
        assertTrue(response.getMessage().contains("Password reset successful"));
        verify(verificationTokenService).verifyPasswordResetToken("reset-token-123");
        verify(passwordEncoder).encode("resetpass123");
        verify(userRepository).save(testUser);
        verify(verificationTokenService).markTokenAsUsed(verificationToken);
    }

    @Test
    void resetPassword_PasswordMismatch() {
        // Arrange
        resetPasswordRequest.setConfirmPassword("differentPassword");

        // Act
        AuthResponse response = authService.resetPassword(resetPasswordRequest);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("New password and confirm password do not match", response.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void resetPassword_InvalidToken() {
        // Arrange
        when(verificationTokenService.verifyPasswordResetToken("reset-token-123")).thenReturn(false);

        // Act
        AuthResponse response = authService.resetPassword(resetPasswordRequest);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Invalid or expired reset token", response.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void isValidToken_ValidToken() {
        // Arrange - Setup token first
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(testUser));
        AuthResponse loginResponse = authService.login(loginRequest);
        String token = loginResponse.getToken();

        // Act
        boolean result = authService.isValidToken(token);

        // Assert
        assertTrue(result);
    }

    @Test
    void isValidToken_InvalidToken() {
        // Act
        boolean result = authService.isValidToken("invalid-token");

        // Assert
        assertFalse(result);
    }

    @Test
    void getUsernameFromToken_ValidToken() {
        // Arrange - Setup token first
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(testUser));
        AuthResponse loginResponse = authService.login(loginRequest);
        String token = loginResponse.getToken();

        // Act
        String username = authService.getUsernameFromToken(token);

        // Assert
        assertEquals("johndoe", username);
    }

    @Test
    void getUsernameFromToken_InvalidToken() {
        // Act
        String username = authService.getUsernameFromToken("invalid-token");

        // Assert
        assertNull(username);
    }
}