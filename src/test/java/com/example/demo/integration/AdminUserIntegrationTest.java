package com.example.demo.integration;

import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import com.example.demo.domain.repository.UserRepository;
import com.example.demo.service.AuthService;
import com.example.demo.domain.dto.LoginRequest;
import com.example.demo.domain.dto.AuthResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AdminUserIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void testSuperAdminUserExists() {
        // Test that super admin user exists in database (created by DataInitializer)
        User superAdminUser = userRepository.findByUsername("superadmin").orElse(null);
        
        assertNotNull(superAdminUser, "Super Admin user should exist");
        assertEquals("superadmin", superAdminUser.getUsername());
        assertTrue(superAdminUser.isActive(), "Super Admin user should be active");
        assertEquals(Role.SUPER_ADMIN, superAdminUser.getRole());
        assertTrue(superAdminUser.isEmailVerified(), "Super Admin user should be email verified");
        assertNotNull(superAdminUser.getPerson(), "Super Admin user should have associated person");
        assertEquals("Super", superAdminUser.getPerson().getFirstName());
        assertEquals("Admin", superAdminUser.getPerson().getLastName());
        assertEquals("superadmin@example.com", superAdminUser.getPerson().getEmail());
    }

    @Test
    public void testSuperAdminPasswordIsCorrect() {
        // Test that super admin password is correctly hashed
        User superAdminUser = userRepository.findByUsername("superadmin").orElse(null);
        
        assertNotNull(superAdminUser, "Super Admin user should exist");
        assertTrue(passwordEncoder.matches("superadmin123", superAdminUser.getPassword()), 
                   "Password 'superadmin123' should match the hashed password");
    }

    @Test
    public void testSuperAdminLoginSuccess() {
        // Test that super admin can login successfully
        LoginRequest loginRequest = new LoginRequest("superadmin", "superadmin123");
        AuthResponse response = authService.login(loginRequest);
        
        assertTrue(response.isSuccess(), "Login should be successful");
        assertEquals("superadmin", response.getUsername());
        assertEquals(Role.SUPER_ADMIN, response.getRole());
        assertNotNull(response.getToken(), "Token should be present");
        assertEquals("Login successful", response.getMessage());
    }

    @Test
    public void testSuperAdminLoginFailureWithWrongPassword() {
        // Test that super admin login fails with wrong password
        LoginRequest loginRequest = new LoginRequest("superadmin", "wrongpassword");
        AuthResponse response = authService.login(loginRequest);
        
        assertFalse(response.isSuccess(), "Login should fail with wrong password");
        assertNull(response.getToken(), "Token should be null on failed login");
        assertNull(response.getRole(), "Role should be null on failed login");
        assertEquals("Invalid username or password", response.getMessage());
    }

    @Test
    public void testRoleSystemIntegration() {
        // Test that role system is properly integrated
        User superAdmin = userRepository.findByUsername("superadmin").orElse(null);
        
        assertNotNull(superAdmin);
        assertEquals(Role.SUPER_ADMIN, superAdmin.getRole());
        
        // Test role name
        assertEquals("SUPER_ADMIN", superAdmin.getRole().getName());
        
        // Test role ordinal (SUPER_ADMIN should be highest)
        assertEquals(2, superAdmin.getRole().ordinal());
        
        // Test role comparison
        assertTrue(superAdmin.getRole().compareTo(Role.ADMIN) > 0);
        assertTrue(superAdmin.getRole().compareTo(Role.USER) > 0);
    }
}