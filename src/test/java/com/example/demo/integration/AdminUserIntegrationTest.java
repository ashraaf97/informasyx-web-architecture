package com.example.demo.integration;

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
@ActiveProfiles("h2")
@Transactional
public class AdminUserIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void testAdminUserExists() {
        // Test that admin user exists in database
        User adminUser = userRepository.findByUsername("admin").orElse(null);
        
        assertNotNull(adminUser, "Admin user should exist");
        assertEquals("admin", adminUser.getUsername());
        assertTrue(adminUser.isActive(), "Admin user should be active");
        assertEquals("ADMIN,USER", adminUser.getRoles());
        assertNotNull(adminUser.getPerson(), "Admin user should have associated person");
        assertEquals("Admin", adminUser.getPerson().getFirstName());
        assertEquals("User", adminUser.getPerson().getLastName());
        assertEquals("admin@example.com", adminUser.getPerson().getEmail());
    }

    @Test
    public void testAdminPasswordIsCorrect() {
        // Test that admin password is correctly hashed
        User adminUser = userRepository.findByUsername("admin").orElse(null);
        
        assertNotNull(adminUser, "Admin user should exist");
        assertTrue(passwordEncoder.matches("admin", adminUser.getPassword()), 
                   "Password 'admin' should match the hashed password");
    }

    @Test
    public void testAdminLoginSuccess() {
        // Test that admin can login successfully
        LoginRequest loginRequest = new LoginRequest("admin", "admin");
        AuthResponse response = authService.login(loginRequest);
        
        assertTrue(response.isSuccess(), "Login should be successful");
        assertEquals("admin", response.getUsername());
        assertNotNull(response.getToken(), "Token should be present");
        assertEquals("Login successful", response.getMessage());
    }

    @Test
    public void testAdminLoginFailureWithWrongPassword() {
        // Test that admin login fails with wrong password
        LoginRequest loginRequest = new LoginRequest("admin", "wrongpassword");
        AuthResponse response = authService.login(loginRequest);
        
        assertFalse(response.isSuccess(), "Login should fail with wrong password");
        assertNull(response.getToken(), "Token should be null on failed login");
        assertEquals("Invalid username or password", response.getMessage());
    }
}