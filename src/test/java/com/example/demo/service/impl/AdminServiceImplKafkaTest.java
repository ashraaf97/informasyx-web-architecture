package com.example.demo.service.impl;

import com.example.demo.domain.Person;
import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import com.example.demo.domain.dto.AdminCreateUserRequest;
import com.example.demo.domain.dto.AuthResponse;
import com.example.demo.domain.dto.ChangeRoleRequest;
import com.example.demo.domain.repository.PersonRepository;
import com.example.demo.domain.repository.UserRepository;
import com.example.demo.event.AdminCreatedEvent;
import com.example.demo.event.UserCreatedEvent;
import com.example.demo.event.UserRoleChangedEvent;
import com.example.demo.security.SecurityUtils;
import com.example.demo.service.EventPublisherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplKafkaTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    private EventPublisherService eventPublisherService;

    @InjectMocks
    private AdminServiceImpl adminService;

    private User mockSuperAdmin;
    private User mockAdmin;
    private Person mockPerson;

    @BeforeEach
    void setUp() {
        // Setup mock super admin
        Person superAdminPerson = new Person();
        superAdminPerson.setId(1L);
        superAdminPerson.setEmail("superadmin@example.com");
        superAdminPerson.setFirstName("Super");
        superAdminPerson.setLastName("Admin");

        mockSuperAdmin = new User();
        mockSuperAdmin.setId(1L);
        mockSuperAdmin.setUsername("superadmin");
        mockSuperAdmin.setRole(Role.SUPER_ADMIN);
        mockSuperAdmin.setPerson(superAdminPerson);

        // Setup mock admin
        Person adminPerson = new Person();
        adminPerson.setId(2L);
        adminPerson.setEmail("admin@example.com");
        adminPerson.setFirstName("Admin");
        adminPerson.setLastName("User");

        mockAdmin = new User();
        mockAdmin.setId(2L);
        mockAdmin.setUsername("admin");
        mockAdmin.setRole(Role.ADMIN);
        mockAdmin.setPerson(adminPerson);

        // Setup mock person for new user
        mockPerson = new Person();
        mockPerson.setId(3L);
        mockPerson.setEmail("newuser@example.com");
        mockPerson.setFirstName("New");
        mockPerson.setLastName("User");
    }

    @Test
    void testCreateUserPublishesUserCreatedEvent() {
        // Arrange
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(mockSuperAdmin));
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(personRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        
        // Mock personRepository.save to return a person with the correct email from the request
        when(personRepository.save(any(Person.class))).thenAnswer(invocation -> {
            Person person = invocation.getArgument(0);
            person.setId(3L);
            return person;
        });
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");

        User savedUser = new User();
        savedUser.setId(10L);
        savedUser.setUsername("testuser");
        savedUser.setRole(Role.USER);
        savedUser.setPerson(mockPerson);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setEmail("testuser@example.com");
        request.setRole(Role.USER);

        // Act
        AuthResponse response = adminService.createUser(request);

        // Assert
        assertTrue(response.isSuccess());
        
        ArgumentCaptor<UserCreatedEvent> eventCaptor = ArgumentCaptor.forClass(UserCreatedEvent.class);
        verify(eventPublisherService, times(1)).publishUserCreatedEvent(eventCaptor.capture());
        
        UserCreatedEvent capturedEvent = eventCaptor.getValue();
        assertEquals(10L, capturedEvent.getUserId());
        assertEquals("testuser", capturedEvent.getUsername());
        assertEquals("testuser@example.com", capturedEvent.getEmail());
        assertEquals("Test", capturedEvent.getFirstName());
        assertEquals("User", capturedEvent.getLastName());
        assertEquals(Role.USER, capturedEvent.getRole());
        assertEquals("superadmin", capturedEvent.getCreatedBy());
        assertNotNull(capturedEvent.getCreatedAt());
    }

    @Test
    void testCreateAdminPublishesAdminCreatedEvent() {
        // Arrange
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(mockSuperAdmin));
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(personRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        
        // Mock personRepository.save to return a person with the correct email from the request
        when(personRepository.save(any(Person.class))).thenAnswer(invocation -> {
            Person person = invocation.getArgument(0);
            person.setId(3L);
            return person;
        });
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");

        User savedAdmin = new User();
        savedAdmin.setId(11L);
        savedAdmin.setUsername("testadmin");
        savedAdmin.setRole(Role.ADMIN);
        savedAdmin.setPerson(mockPerson);
        when(userRepository.save(any(User.class))).thenReturn(savedAdmin);

        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setUsername("testadmin");
        request.setPassword("password123");
        request.setFirstName("Test");
        request.setLastName("Admin");
        request.setEmail("testadmin@example.com");
        request.setRole(Role.ADMIN);

        // Act
        AuthResponse response = adminService.createUser(request);

        // Assert
        assertTrue(response.isSuccess());
        
        ArgumentCaptor<AdminCreatedEvent> eventCaptor = ArgumentCaptor.forClass(AdminCreatedEvent.class);
        verify(eventPublisherService, times(1)).publishAdminCreatedEvent(eventCaptor.capture());
        
        AdminCreatedEvent capturedEvent = eventCaptor.getValue();
        assertEquals(11L, capturedEvent.getAdminId());
        assertEquals("testadmin", capturedEvent.getUsername());
        assertEquals("testadmin@example.com", capturedEvent.getEmail());
        assertEquals("Test", capturedEvent.getFirstName());
        assertEquals("Admin", capturedEvent.getLastName());
        assertEquals(Role.ADMIN, capturedEvent.getRole());
        assertEquals("superadmin", capturedEvent.getCreatedBy());
        assertEquals("SUPER_ADMIN", capturedEvent.getCreatedByRole());
        assertNotNull(capturedEvent.getCreatedAt());
    }

    @Test
    void testChangeUserRolePublishesUserRoleChangedEvent() {
        // Arrange
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(mockSuperAdmin));

        User targetUser = new User();
        targetUser.setId(12L);
        targetUser.setUsername("targetuser");
        targetUser.setRole(Role.USER);
        when(userRepository.findByUsername("targetuser")).thenReturn(Optional.of(targetUser));

        User updatedUser = new User();
        updatedUser.setId(12L);
        updatedUser.setUsername("targetuser");
        updatedUser.setRole(Role.ADMIN);
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        ChangeRoleRequest request = new ChangeRoleRequest();
        request.setUsername("targetuser");
        request.setRole(Role.ADMIN);

        // Act
        AuthResponse response = adminService.changeUserRole(request);

        // Assert
        assertTrue(response.isSuccess());
        
        ArgumentCaptor<UserRoleChangedEvent> eventCaptor = ArgumentCaptor.forClass(UserRoleChangedEvent.class);
        verify(eventPublisherService, times(1)).publishUserRoleChangedEvent(eventCaptor.capture());
        
        UserRoleChangedEvent capturedEvent = eventCaptor.getValue();
        assertEquals(12L, capturedEvent.getUserId());
        assertEquals("targetuser", capturedEvent.getUsername());
        assertEquals(Role.USER, capturedEvent.getPreviousRole());
        assertEquals(Role.ADMIN, capturedEvent.getNewRole());
        assertEquals("superadmin", capturedEvent.getChangedBy());
        assertEquals("SUPER_ADMIN", capturedEvent.getChangedByRole());
        assertNotNull(capturedEvent.getChangedAt());
    }

    @Test
    void testCreateUserByAdminPublishesCorrectCreatedBy() {
        // Arrange - Use regular admin instead of super admin
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(mockAdmin));
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(personRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(personRepository.save(any(Person.class))).thenReturn(mockPerson);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");

        User savedUser = new User();
        savedUser.setId(13L);
        savedUser.setUsername("adminuser");
        savedUser.setRole(Role.USER);
        savedUser.setPerson(mockPerson);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setUsername("adminuser");
        request.setPassword("password123");
        request.setFirstName("Admin");
        request.setLastName("Created");
        request.setEmail("admincreated@example.com");
        request.setRole(Role.USER);

        // Act
        AuthResponse response = adminService.createUser(request);

        // Assert
        assertTrue(response.isSuccess());
        
        ArgumentCaptor<UserCreatedEvent> eventCaptor = ArgumentCaptor.forClass(UserCreatedEvent.class);
        verify(eventPublisherService, times(1)).publishUserCreatedEvent(eventCaptor.capture());
        
        UserCreatedEvent capturedEvent = eventCaptor.getValue();
        assertEquals("admin", capturedEvent.getCreatedBy());
    }

    @Test
    void testFailedUserCreationDoesNotPublishEvent() {
        // Arrange - Username already exists
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(mockSuperAdmin));
        when(userRepository.findByUsername("existinguser")).thenReturn(Optional.of(new User()));

        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setUsername("existinguser");
        request.setPassword("password123");
        request.setFirstName("Existing");
        request.setLastName("User");
        request.setEmail("existing@example.com");
        request.setRole(Role.USER);

        // Act
        AuthResponse response = adminService.createUser(request);

        // Assert
        assertFalse(response.isSuccess());
        verify(eventPublisherService, never()).publishUserCreatedEvent(any());
        verify(eventPublisherService, never()).publishAdminCreatedEvent(any());
    }

    @Test
    void testFailedRoleChangeDoesNotPublishEvent() {
        // Arrange - User not found
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(mockSuperAdmin));
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        ChangeRoleRequest request = new ChangeRoleRequest();
        request.setUsername("nonexistent");
        request.setRole(Role.ADMIN);

        // Act
        AuthResponse response = adminService.changeUserRole(request);

        // Assert
        assertFalse(response.isSuccess());
        verify(eventPublisherService, never()).publishUserRoleChangedEvent(any());
    }

    @Test
    void testUnauthorizedUserCreationDoesNotPublishEvent() {
        // Arrange - No current user
        when(securityUtils.getCurrentUser()).thenReturn(Optional.empty());

        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setEmail("test@example.com");
        request.setRole(Role.USER);

        // Act
        AuthResponse response = adminService.createUser(request);

        // Assert
        assertFalse(response.isSuccess());
        verify(eventPublisherService, never()).publishUserCreatedEvent(any());
        verify(eventPublisherService, never()).publishAdminCreatedEvent(any());
    }

    @Test
    void testCreateUserWithExceptionDoesNotPublishEvent() {
        // Arrange
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(mockSuperAdmin));
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(personRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(personRepository.save(any(Person.class))).thenThrow(new RuntimeException("Database error"));

        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setEmail("test@example.com");
        request.setRole(Role.USER);

        // Act
        AuthResponse response = adminService.createUser(request);

        // Assert
        assertFalse(response.isSuccess());
        verify(eventPublisherService, never()).publishUserCreatedEvent(any());
        verify(eventPublisherService, never()).publishAdminCreatedEvent(any());
    }

    @Test
    void testAdminCannotCreateSuperAdminDoesNotPublishEvent() {
        // Arrange
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(mockAdmin));

        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setUsername("superadmin");
        request.setPassword("password123");
        request.setFirstName("Super");
        request.setLastName("Admin");
        request.setEmail("super@example.com");
        request.setRole(Role.SUPER_ADMIN);

        // Act
        AuthResponse response = adminService.createUser(request);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Cannot create Super Admin users", response.getMessage());
        verify(eventPublisherService, never()).publishUserCreatedEvent(any());
        verify(eventPublisherService, never()).publishAdminCreatedEvent(any());
    }

    @Test
    void testAdminCannotCreateAdminDoesNotPublishEvent() {
        // Arrange
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(mockAdmin));

        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setUsername("newadmin");
        request.setPassword("password123");
        request.setFirstName("New");
        request.setLastName("Admin");
        request.setEmail("newadmin@example.com");
        request.setRole(Role.ADMIN);

        // Act
        AuthResponse response = adminService.createUser(request);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Only Super Admin can create Admin users", response.getMessage());
        verify(eventPublisherService, never()).publishUserCreatedEvent(any());
        verify(eventPublisherService, never()).publishAdminCreatedEvent(any());
    }

    @Test
    void testEventPublisherServiceFailureDoesNotAffectResponse() {
        // Arrange
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(mockSuperAdmin));
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(personRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(personRepository.save(any(Person.class))).thenReturn(mockPerson);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");

        User savedUser = new User();
        savedUser.setId(14L);
        savedUser.setUsername("eventfailuser");
        savedUser.setRole(Role.USER);
        savedUser.setPerson(mockPerson);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Mock event publisher to throw exception
        doThrow(new RuntimeException("Kafka failure")).when(eventPublisherService).publishUserCreatedEvent(any());

        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setUsername("eventfailuser");
        request.setPassword("password123");
        request.setFirstName("Event");
        request.setLastName("Fail");
        request.setEmail("eventfail@example.com");
        request.setRole(Role.USER);

        // Act
        AuthResponse response = adminService.createUser(request);
        
        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Failed to create user", response.getMessage());
        
        // Verify the event publisher was called
        verify(eventPublisherService, times(1)).publishUserCreatedEvent(any());
    }
}