package com.example.demo.service.impl;

import com.example.demo.domain.Person;
import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import com.example.demo.domain.dto.AdminCreateUserRequest;
import com.example.demo.domain.dto.AuthResponse;
import com.example.demo.domain.dto.ChangeRoleRequest;
import com.example.demo.domain.repository.PersonRepository;
import com.example.demo.domain.repository.UserRepository;
import com.example.demo.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private AdminServiceImpl adminService;

    private User superAdminUser;
    private User adminUser;
    private User regularUser;
    private Person person;

    @BeforeEach
    void setUp() {
        // Setup test users
        person = new Person();
        person.setId(1L);
        person.setFirstName("Test");
        person.setLastName("User");
        person.setEmail("test@example.com");

        superAdminUser = new User();
        superAdminUser.setId(1L);
        superAdminUser.setUsername("superadmin");
        superAdminUser.setRole(Role.SUPER_ADMIN);
        superAdminUser.setPerson(person);

        adminUser = new User();
        adminUser.setId(2L);
        adminUser.setUsername("admin");
        adminUser.setRole(Role.ADMIN);
        adminUser.setPerson(person);

        regularUser = new User();
        regularUser.setId(3L);
        regularUser.setUsername("user");
        regularUser.setRole(Role.USER);
        regularUser.setPerson(person);
    }

    @Test
    void createUser_SuperAdminCreatesUser_Success() {
        // Arrange
        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setUsername("newuser");
        request.setPassword("password123");
        request.setFirstName("New");
        request.setLastName("User");
        request.setEmail("newuser@example.com");
        request.setRole(Role.USER);

        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(superAdminUser));
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(personRepository.findByEmail("newuser@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(personRepository.save(any(Person.class))).thenReturn(person);
        when(userRepository.save(any(User.class))).thenReturn(regularUser);

        // Act
        AuthResponse response = adminService.createUser(request);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("newuser", response.getUsername());
        assertTrue(response.getMessage().contains("User created successfully"));
        verify(userRepository).save(any(User.class));
        verify(personRepository).save(any(Person.class));
    }

    @Test
    void createUser_AdminCreatesUser_Success() {
        // Arrange
        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setUsername("newuser");
        request.setPassword("password123");
        request.setFirstName("New");
        request.setLastName("User");
        request.setEmail("newuser@example.com");
        request.setRole(Role.USER);

        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(adminUser));
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(personRepository.findByEmail("newuser@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(personRepository.save(any(Person.class))).thenReturn(person);
        when(userRepository.save(any(User.class))).thenReturn(regularUser);

        // Act
        AuthResponse response = adminService.createUser(request);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("newuser", response.getUsername());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_AdminTriesToCreateAdmin_Failure() {
        // Arrange
        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setUsername("newadmin");
        request.setPassword("password123");
        request.setFirstName("New");
        request.setLastName("Admin");
        request.setEmail("newadmin@example.com");
        request.setRole(Role.ADMIN);

        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(adminUser));

        // Act
        AuthResponse response = adminService.createUser(request);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Only Super Admin can create Admin users", response.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_TriesToCreateSuperAdmin_Failure() {
        // Arrange
        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setUsername("newsuperadmin");
        request.setRole(Role.SUPER_ADMIN);

        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(superAdminUser));

        // Act
        AuthResponse response = adminService.createUser(request);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Cannot create Super Admin users", response.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_UsernameExists_Failure() {
        // Arrange
        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setUsername("existinguser");
        request.setRole(Role.USER);

        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(superAdminUser));
        when(userRepository.findByUsername("existinguser")).thenReturn(Optional.of(regularUser));

        // Act
        AuthResponse response = adminService.createUser(request);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Username already exists", response.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_EmailExists_Failure() {
        // Arrange
        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setUsername("newuser");
        request.setEmail("existing@example.com");
        request.setRole(Role.USER);

        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(superAdminUser));
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(personRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(person));

        // Act
        AuthResponse response = adminService.createUser(request);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Email already exists", response.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_Unauthorized_Failure() {
        // Arrange
        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setUsername("newuser");
        request.setRole(Role.USER);

        when(securityUtils.getCurrentUser()).thenReturn(Optional.empty());

        // Act
        AuthResponse response = adminService.createUser(request);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Unauthorized", response.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void changeUserRole_SuperAdminChangesRole_Success() {
        // Arrange
        ChangeRoleRequest request = new ChangeRoleRequest();
        request.setUsername("user");
        request.setRole(Role.ADMIN);

        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(superAdminUser));
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(regularUser));
        when(userRepository.save(any(User.class))).thenReturn(regularUser);

        // Act
        AuthResponse response = adminService.changeUserRole(request);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("user", response.getUsername());
        assertTrue(response.getMessage().contains("User role changed from USER to ADMIN"));
        verify(userRepository).save(regularUser);
        assertEquals(Role.ADMIN, regularUser.getRole());
    }

    @Test
    void changeUserRole_AdminTriesToChangeRole_Failure() {
        // Arrange
        ChangeRoleRequest request = new ChangeRoleRequest();
        request.setUsername("user");
        request.setRole(Role.ADMIN);

        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(adminUser));

        // Act
        AuthResponse response = adminService.changeUserRole(request);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Only Super Admin can change user roles", response.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void changeUserRole_TriesToPromoteToSuperAdmin_Failure() {
        // Arrange
        ChangeRoleRequest request = new ChangeRoleRequest();
        request.setUsername("user");
        request.setRole(Role.SUPER_ADMIN);

        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(superAdminUser));

        // Act
        AuthResponse response = adminService.changeUserRole(request);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Cannot promote users to Super Admin", response.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void changeUserRole_TriesToChangeSuperAdminRole_Failure() {
        // Arrange
        ChangeRoleRequest request = new ChangeRoleRequest();
        request.setUsername("superadmin");
        request.setRole(Role.ADMIN);

        User targetSuperAdmin = new User();
        targetSuperAdmin.setUsername("superadmin");
        targetSuperAdmin.setRole(Role.SUPER_ADMIN);

        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(superAdminUser));
        when(userRepository.findByUsername("superadmin")).thenReturn(Optional.of(targetSuperAdmin));

        // Act
        AuthResponse response = adminService.changeUserRole(request);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Cannot change Super Admin role", response.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void changeUserRole_UserNotFound_Failure() {
        // Arrange
        ChangeRoleRequest request = new ChangeRoleRequest();
        request.setUsername("nonexistent");
        request.setRole(Role.ADMIN);

        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(superAdminUser));
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act
        AuthResponse response = adminService.changeUserRole(request);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("User not found", response.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void changeUserRole_Unauthorized_Failure() {
        // Arrange
        ChangeRoleRequest request = new ChangeRoleRequest();
        request.setUsername("user");
        request.setRole(Role.ADMIN);

        when(securityUtils.getCurrentUser()).thenReturn(Optional.empty());

        // Act
        AuthResponse response = adminService.changeUserRole(request);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Unauthorized", response.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }
}