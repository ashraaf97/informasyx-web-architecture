package com.example.demo.service;

import com.example.demo.domain.Person;
import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import com.example.demo.domain.dto.AuthResponse;
import com.example.demo.domain.dto.LoginRequest;
import com.example.demo.domain.dto.SignUpRequest;
import com.example.demo.domain.repository.PersonRepository;
import com.example.demo.domain.repository.UserRepository;
import com.example.demo.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DirtiesContext
@Transactional
public class AuthServiceRoleTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private AuthServiceImpl authServiceImpl;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        // Create test users with different roles
        createTestUsers();
    }

    private void createTestUsers() {
        // Create Super Admin
        Person superAdminPerson = new Person();
        superAdminPerson.setFirstName("Super");
        superAdminPerson.setLastName("Admin");
        superAdminPerson.setEmail("superadmin@test.com");
        superAdminPerson = personRepository.save(superAdminPerson);

        User superAdmin = new User();
        superAdmin.setUsername("testsuperadmin");
        superAdmin.setPassword(passwordEncoder.encode("password123"));
        superAdmin.setPerson(superAdminPerson);
        superAdmin.setRole(Role.SUPER_ADMIN);
        superAdmin.setActive(true);
        superAdmin.setEmailVerified(true);
        userRepository.save(superAdmin);

        // Create Admin
        Person adminPerson = new Person();
        adminPerson.setFirstName("Test");
        adminPerson.setLastName("Admin");
        adminPerson.setEmail("admin@test.com");
        adminPerson = personRepository.save(adminPerson);

        User admin = new User();
        admin.setUsername("testadmin");
        admin.setPassword(passwordEncoder.encode("password123"));
        admin.setPerson(adminPerson);
        admin.setRole(Role.ADMIN);
        admin.setActive(true);
        admin.setEmailVerified(true);
        userRepository.save(admin);

        // Create Regular User
        Person userPerson = new Person();
        userPerson.setFirstName("Test");
        userPerson.setLastName("User");
        userPerson.setEmail("user@test.com");
        userPerson = personRepository.save(userPerson);

        User user = new User();
        user.setUsername("testuser");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setPerson(userPerson);
        user.setRole(Role.USER);
        user.setActive(true);
        user.setEmailVerified(true);
        userRepository.save(user);
    }

    @Test
    void login_SuperAdminUser_ReturnsRoleInResponse() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("testsuperadmin", "password123");

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("testsuperadmin", response.getUsername());
        assertEquals(Role.SUPER_ADMIN, response.getRole());
        assertNotNull(response.getToken());
        assertEquals("Login successful", response.getMessage());
    }

    @Test
    void login_AdminUser_ReturnsRoleInResponse() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("testadmin", "password123");

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("testadmin", response.getUsername());
        assertEquals(Role.ADMIN, response.getRole());
        assertNotNull(response.getToken());
        assertEquals("Login successful", response.getMessage());
    }

    @Test
    void login_RegularUser_ReturnsRoleInResponse() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("testuser", "password123");

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("testuser", response.getUsername());
        assertEquals(Role.USER, response.getRole());
        assertNotNull(response.getToken());
        assertEquals("Login successful", response.getMessage());
    }

    @Test
    void login_InvalidCredentials_NoRoleInResponse() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("testuser", "wrongpassword");

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertFalse(response.isSuccess());
        assertNull(response.getUsername());
        assertNull(response.getRole());
        assertNull(response.getToken());
        assertEquals("Invalid username or password", response.getMessage());
    }

    @Test
    void login_NonExistentUser_NoRoleInResponse() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("nonexistent", "password123");

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertFalse(response.isSuccess());
        assertNull(response.getUsername());
        assertNull(response.getRole());
        assertNull(response.getToken());
        assertEquals("Invalid username or password", response.getMessage());
    }

    @Test
    void signUp_NewUser_AssignedUserRole() {
        // Arrange
        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setUsername("newuser");
        signUpRequest.setPassword("password123");
        signUpRequest.setConfirmPassword("password123");
        signUpRequest.setFirstName("New");
        signUpRequest.setLastName("User");
        signUpRequest.setEmail("newuser@test.com");
        signUpRequest.setPhoneNumber("1234567890");
        signUpRequest.setAddress("123 New St");

        // Act
        AuthResponse response = authService.signUp(signUpRequest);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("newuser", response.getUsername());

        // Verify user was created with USER role
        User createdUser = userRepository.findByUsername("newuser").orElse(null);
        assertNotNull(createdUser);
        assertEquals(Role.USER, createdUser.getRole());
        assertFalse(createdUser.isEmailVerified()); // Should be false until verified
    }

    @Test
    void login_DeactivatedUser_FailsRegardlessOfRole() {
        // Arrange - Create a deactivated admin user
        Person deactivatedPerson = new Person();
        deactivatedPerson.setFirstName("Deactivated");
        deactivatedPerson.setLastName("Admin");
        deactivatedPerson.setEmail("deactivated@test.com");
        deactivatedPerson = personRepository.save(deactivatedPerson);

        User deactivatedAdmin = new User();
        deactivatedAdmin.setUsername("deactivatedadmin");
        deactivatedAdmin.setPassword(passwordEncoder.encode("password123"));
        deactivatedAdmin.setPerson(deactivatedPerson);
        deactivatedAdmin.setRole(Role.ADMIN);
        deactivatedAdmin.setActive(false); // Deactivated
        deactivatedAdmin.setEmailVerified(true);
        userRepository.save(deactivatedAdmin);

        LoginRequest loginRequest = new LoginRequest("deactivatedadmin", "password123");

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("User account is deactivated", response.getMessage());
        assertNull(response.getRole());
    }

    @Test
    void login_UnverifiedUser_FailsRegardlessOfRole() {
        // Arrange - Create an unverified super admin user
        Person unverifiedPerson = new Person();
        unverifiedPerson.setFirstName("Unverified");
        unverifiedPerson.setLastName("SuperAdmin");
        unverifiedPerson.setEmail("unverified@test.com");
        unverifiedPerson = personRepository.save(unverifiedPerson);

        User unverifiedSuperAdmin = new User();
        unverifiedSuperAdmin.setUsername("unverifiedsuperadmin");
        unverifiedSuperAdmin.setPassword(passwordEncoder.encode("password123"));
        unverifiedSuperAdmin.setPerson(unverifiedPerson);
        unverifiedSuperAdmin.setRole(Role.SUPER_ADMIN);
        unverifiedSuperAdmin.setActive(true);
        unverifiedSuperAdmin.setEmailVerified(false); // Not verified
        userRepository.save(unverifiedSuperAdmin);

        LoginRequest loginRequest = new LoginRequest("unverifiedsuperadmin", "password123");

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Please verify your email address before logging in", response.getMessage());
        assertNull(response.getRole());
    }

    @Test
    void login_ValidToken_CanRetrieveUserAndRole() {
        // Arrange - Login first to get token
        LoginRequest loginRequest = new LoginRequest("testadmin", "password123");
        AuthResponse loginResponse = authService.login(loginRequest);
        assertTrue(loginResponse.isSuccess());
        String token = loginResponse.getToken();

        // Act - Check token validity and user retrieval
        boolean isValidToken = authServiceImpl.isValidToken(token);
        String username = authServiceImpl.getUsernameFromToken(token);

        // Assert
        assertTrue(isValidToken);
        assertEquals("testadmin", username);

        // Verify user can be retrieved with correct role
        User user = userRepository.findByUsername(username).orElse(null);
        assertNotNull(user);
        assertEquals(Role.ADMIN, user.getRole());
        assertEquals("testadmin", user.getUsername());
    }

    @Test
    void logout_ValidToken_Success() {
        // Arrange - Login first to get token
        LoginRequest loginRequest = new LoginRequest("testuser", "password123");
        AuthResponse loginResponse = authService.login(loginRequest);
        assertTrue(loginResponse.isSuccess());
        String token = loginResponse.getToken();

        // Act
        AuthResponse logoutResponse = authService.logout(token);

        // Assert
        assertTrue(logoutResponse.isSuccess());
        assertEquals("testuser", logoutResponse.getUsername());
        assertEquals("Logout successful", logoutResponse.getMessage());

        // Verify token is no longer valid
        assertFalse(authServiceImpl.isValidToken(token));
    }

    @Test
    void authenticationFlow_AllRoles_WorkCorrectly() {
        // Test complete authentication flow for all roles
        
        // Super Admin
        AuthResponse superAdminLogin = authService.login(new LoginRequest("testsuperadmin", "password123"));
        assertTrue(superAdminLogin.isSuccess());
        assertEquals(Role.SUPER_ADMIN, superAdminLogin.getRole());
        
        AuthResponse superAdminLogout = authService.logout(superAdminLogin.getToken());
        assertTrue(superAdminLogout.isSuccess());
        
        // Admin
        AuthResponse adminLogin = authService.login(new LoginRequest("testadmin", "password123"));
        assertTrue(adminLogin.isSuccess());
        assertEquals(Role.ADMIN, adminLogin.getRole());
        
        AuthResponse adminLogout = authService.logout(adminLogin.getToken());
        assertTrue(adminLogout.isSuccess());
        
        // User
        AuthResponse userLogin = authService.login(new LoginRequest("testuser", "password123"));
        assertTrue(userLogin.isSuccess());
        assertEquals(Role.USER, userLogin.getRole());
        
        AuthResponse userLogout = authService.logout(userLogin.getToken());
        assertTrue(userLogout.isSuccess());
    }
}