package com.example.demo.controller;

import com.example.demo.domain.Person;
import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import com.example.demo.domain.dto.*;
import com.example.demo.domain.repository.PersonRepository;
import com.example.demo.domain.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private User testUser;
    private Person testPerson;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Clean up any existing test data
        userRepository.deleteAll();
        personRepository.deleteAll();

        // Create test person and user
        testPerson = new Person();
        testPerson.setFirstName("John");
        testPerson.setLastName("Doe");
        testPerson.setEmail("john.doe@example.com");
        testPerson.setPhoneNumber("1234567890");
        testPerson.setAddress("123 Main St");

        testUser = new User();
        testUser.setUsername("johndoe");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setPerson(testPerson);
        testUser.setActive(true);
        testUser.setEmailVerified(true);
        testUser.setRole(Role.USER);

        testPerson.setUser(testUser);
        personRepository.save(testPerson);
        userRepository.save(testUser);
    }

    @Test
    void signUp_Success() throws Exception {
        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setUsername("newuser");
        signUpRequest.setPassword("newpass123");
        signUpRequest.setConfirmPassword("newpass123");
        signUpRequest.setFirstName("Jane");
        signUpRequest.setLastName("Smith");
        signUpRequest.setEmail("jane.smith@example.com");
        signUpRequest.setPhoneNumber("0987654321");
        signUpRequest.setAddress("456 Oak Ave");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.message").value("User registered successfully! Please check your email to verify your account."));
    }

    @Test
    void signUp_UsernameAlreadyExists() throws Exception {
        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setUsername("johndoe"); // Username already exists
        signUpRequest.setPassword("newpass123");
        signUpRequest.setConfirmPassword("newpass123");
        signUpRequest.setFirstName("Jane");
        signUpRequest.setLastName("Smith");
        signUpRequest.setEmail("jane.smith@example.com");
        signUpRequest.setPhoneNumber("0987654321");
        signUpRequest.setAddress("456 Oak Ave");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Username already exists"));
    }

    @Test
    void signUp_EmailAlreadyExists() throws Exception {
        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setUsername("newuser");
        signUpRequest.setPassword("newpass123");
        signUpRequest.setConfirmPassword("newpass123");
        signUpRequest.setFirstName("Jane");
        signUpRequest.setLastName("Smith");
        signUpRequest.setEmail("john.doe@example.com"); // Email already exists
        signUpRequest.setPhoneNumber("0987654321");
        signUpRequest.setAddress("456 Oak Ave");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email already exists"));
    }

    @Test
    void signUp_PasswordMismatch() throws Exception {
        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setUsername("newuser");
        signUpRequest.setPassword("newpass123");
        signUpRequest.setConfirmPassword("differentpass123"); // Passwords don't match
        signUpRequest.setFirstName("Jane");
        signUpRequest.setLastName("Smith");
        signUpRequest.setEmail("jane.smith@example.com");
        signUpRequest.setPhoneNumber("0987654321");
        signUpRequest.setAddress("456 Oak Ave");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Password and confirm password do not match"));
    }

    @Test
    void login_Success() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("johndoe");
        loginRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.username").value("johndoe"))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.message").value("Login successful"));
    }

    @Test
    void login_InvalidCredentials() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("johndoe");
        loginRequest.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    void login_UserNotVerified() throws Exception {
        // Create unverified user
        testUser.setEmailVerified(false);
        userRepository.save(testUser);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("johndoe");
        loginRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Please verify your email address before logging in"));
    }

    @Test
    void login_UserNotActive() throws Exception {
        // Create inactive user
        testUser.setActive(false);
        userRepository.save(testUser);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("johndoe");
        loginRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User account is deactivated"));
    }

    @Test
    void logout_Success() throws Exception {
        // First login to get a token
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("johndoe");
        loginRequest.setPassword("password123");

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        AuthResponse authResponse = objectMapper.readValue(loginResponse, AuthResponse.class);
        String token = authResponse.getToken();

        // Now logout
        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Logout successful"));
    }

    @Test
    void logout_InvalidToken() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid token"));
    }

    @Test
    void forgotPassword_Success() throws Exception {
        ForgotPasswordRequest forgotPasswordRequest = new ForgotPasswordRequest();
        forgotPasswordRequest.setEmail("john.doe@example.com");

        mockMvc.perform(post("/api/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(forgotPasswordRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("If an account with this email exists, you will receive password reset instructions."));
    }

    @Test
    void forgotPassword_EmailNotFound() throws Exception {
        ForgotPasswordRequest forgotPasswordRequest = new ForgotPasswordRequest();
        forgotPasswordRequest.setEmail("nonexistent@example.com");

        mockMvc.perform(post("/api/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(forgotPasswordRequest)))
                .andExpect(status().isOk()) // Always returns OK for security
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("If an account with this email exists, you will receive password reset instructions."));
    }

    @Test
    void changePassword_Success() throws Exception {
        // First login to get a token
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("johndoe");
        loginRequest.setPassword("password123");

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        AuthResponse authResponse = objectMapper.readValue(loginResponse, AuthResponse.class);
        String token = authResponse.getToken();

        // Change password
        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest();
        changePasswordRequest.setCurrentPassword("password123");
        changePasswordRequest.setNewPassword("newpassword123");
        changePasswordRequest.setConfirmPassword("newpassword123");

        mockMvc.perform(put("/api/auth/change-password")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(changePasswordRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Password changed successfully"));
    }

    @Test
    void changePassword_InvalidCurrentPassword() throws Exception {
        // First login to get a token
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("johndoe");
        loginRequest.setPassword("password123");

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        AuthResponse authResponse = objectMapper.readValue(loginResponse, AuthResponse.class);
        String token = authResponse.getToken();

        // Change password with wrong current password
        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest();
        changePasswordRequest.setCurrentPassword("wrongpassword");
        changePasswordRequest.setNewPassword("newpassword123");
        changePasswordRequest.setConfirmPassword("newpassword123");

        mockMvc.perform(put("/api/auth/change-password")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(changePasswordRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Current password is incorrect"));
    }

    @Test
    void changePassword_NewPasswordMismatch() throws Exception {
        // First login to get a token
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("johndoe");
        loginRequest.setPassword("password123");

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        AuthResponse authResponse = objectMapper.readValue(loginResponse, AuthResponse.class);
        String token = authResponse.getToken();

        // Change password with mismatched new passwords
        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest();
        changePasswordRequest.setCurrentPassword("password123");
        changePasswordRequest.setNewPassword("newpassword123");
        changePasswordRequest.setConfirmPassword("differentpassword123");

        mockMvc.perform(put("/api/auth/change-password")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(changePasswordRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("New password and confirm password do not match"));
    }

    @Test
    void verifyEmail_WithToken() throws Exception {
        mockMvc.perform(post("/api/auth/verify-email")
                .param("token", "test-verification-token"))
                .andExpect(status().isBadRequest()) // Will fail due to invalid token, but tests endpoint structure
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void resetPassword_WithValidRequest() throws Exception {
        ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest();
        resetPasswordRequest.setToken("test-reset-token");
        resetPasswordRequest.setNewPassword("newpassword123");
        resetPasswordRequest.setConfirmPassword("newpassword123");

        mockMvc.perform(post("/api/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resetPasswordRequest)))
                .andExpect(status().isBadRequest()) // Will fail due to invalid token, but tests endpoint structure
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void resetPassword_PasswordMismatch() throws Exception {
        ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest();
        resetPasswordRequest.setToken("test-reset-token");
        resetPasswordRequest.setNewPassword("newpassword123");
        resetPasswordRequest.setConfirmPassword("differentpassword123");

        mockMvc.perform(post("/api/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resetPasswordRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("New password and confirm password do not match"));
    }

    @Test
    void invalidJsonRequest_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("invalid json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void missingRequiredFields_ReturnsBadRequest() throws Exception {
        LoginRequest incompleteRequest = new LoginRequest();
        incompleteRequest.setUsername(""); // Empty username

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(incompleteRequest)))
                .andExpect(status().isBadRequest());
    }
}