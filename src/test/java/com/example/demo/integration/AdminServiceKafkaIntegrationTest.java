package com.example.demo.integration;

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
import com.example.demo.service.AdminService;
import com.example.demo.service.EventPublisherService;
import com.example.demo.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-integration.properties", properties = {
    "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
    "app.kafka.topics.user-events=test-user-events",
    "app.kafka.topics.admin-events=test-admin-events"
})
@EmbeddedKafka(partitions = 1, topics = {"test-user-events", "test-admin-events"})
@DirtiesContext
@Transactional
class AdminServiceKafkaIntegrationTest {

    @SpyBean
    private EventPublisherService eventPublisherService;

    @MockBean
    private SecurityUtils securityUtils;

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User superAdminUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        // Create super admin user for testing
        Person superAdminPerson = new Person();
        superAdminPerson.setFirstName("Super");
        superAdminPerson.setLastName("Admin");
        superAdminPerson.setEmail("superadmin@test.com");
        superAdminPerson = personRepository.save(superAdminPerson);

        superAdminUser = new User();
        superAdminUser.setUsername("testsuperadmin");
        superAdminUser.setPassword(passwordEncoder.encode("password"));
        superAdminUser.setPerson(superAdminPerson);
        superAdminUser.setRole(Role.SUPER_ADMIN);
        superAdminUser.setActive(true);
        superAdminUser.setEmailVerified(true);
        superAdminUser = userRepository.save(superAdminUser);

        // Create admin user for testing
        Person adminPerson = new Person();
        adminPerson.setFirstName("Test");
        adminPerson.setLastName("Admin");
        adminPerson.setEmail("testadmin@test.com");
        adminPerson = personRepository.save(adminPerson);

        adminUser = new User();
        adminUser.setUsername("testadmin");
        adminUser.setPassword(passwordEncoder.encode("password"));
        adminUser.setPerson(adminPerson);
        adminUser.setRole(Role.ADMIN);
        adminUser.setActive(true);
        adminUser.setEmailVerified(true);
        adminUser = userRepository.save(adminUser);
    }

    private void authenticateAsSuperAdmin() {
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(superAdminUser));
    }

    private void authenticateAsAdmin() {
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(adminUser));
    }

    @Test
    void testCreateUserPublishesUserCreatedEvent() {
        // Arrange
        authenticateAsSuperAdmin();
        
        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setUsername("testuser123");
        request.setPassword("password123");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setEmail("testuser123@example.com");
        request.setRole(Role.USER);

        // Act
        AuthResponse response = adminService.createUser(request);

        // Assert
        assertTrue(response.isSuccess());
        
        ArgumentCaptor<UserCreatedEvent> eventCaptor = ArgumentCaptor.forClass(UserCreatedEvent.class);
        verify(eventPublisherService, times(1)).publishUserCreatedEvent(eventCaptor.capture());
        
        UserCreatedEvent capturedEvent = eventCaptor.getValue();
        assertNotNull(capturedEvent);
        assertEquals("testuser123", capturedEvent.getUsername());
        assertEquals("testuser123@example.com", capturedEvent.getEmail());
        assertEquals("Test", capturedEvent.getFirstName());
        assertEquals("User", capturedEvent.getLastName());
        assertEquals(Role.USER, capturedEvent.getRole());
        assertEquals("testsuperadmin", capturedEvent.getCreatedBy());
        assertEquals("USER_CREATED", capturedEvent.getEventType());
        assertNotNull(capturedEvent.getUserId());
        assertNotNull(capturedEvent.getCreatedAt());
    }

    @Test
    void testCreateAdminPublishesAdminCreatedEvent() {
        // Arrange
        authenticateAsSuperAdmin();
        
        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setUsername("testadmin123");
        request.setPassword("password123");
        request.setFirstName("Test");
        request.setLastName("Admin");
        request.setEmail("testadmin123@example.com");
        request.setRole(Role.ADMIN);

        // Act
        AuthResponse response = adminService.createUser(request);

        // Assert
        assertTrue(response.isSuccess());
        
        ArgumentCaptor<AdminCreatedEvent> eventCaptor = ArgumentCaptor.forClass(AdminCreatedEvent.class);
        verify(eventPublisherService, times(1)).publishAdminCreatedEvent(eventCaptor.capture());
        
        AdminCreatedEvent capturedEvent = eventCaptor.getValue();
        assertNotNull(capturedEvent);
        assertEquals("testadmin123", capturedEvent.getUsername());
        assertEquals("testadmin123@example.com", capturedEvent.getEmail());
        assertEquals("Test", capturedEvent.getFirstName());
        assertEquals("Admin", capturedEvent.getLastName());
        assertEquals(Role.ADMIN, capturedEvent.getRole());
        assertEquals("testsuperadmin", capturedEvent.getCreatedBy());
        assertEquals("SUPER_ADMIN", capturedEvent.getCreatedByRole());
        assertEquals("ADMIN_CREATED", capturedEvent.getEventType());
        assertNotNull(capturedEvent.getAdminId());
        assertNotNull(capturedEvent.getCreatedAt());
    }

    @Test
    void testChangeUserRolePublishesUserRoleChangedEvent() {
        // Arrange
        authenticateAsSuperAdmin();
        
        // First create a regular user
        AdminCreateUserRequest createRequest = new AdminCreateUserRequest();
        createRequest.setUsername("rolechangeuser");
        createRequest.setPassword("password123");
        createRequest.setFirstName("RoleChange");
        createRequest.setLastName("User");
        createRequest.setEmail("rolechangeuser@example.com");
        createRequest.setRole(Role.USER);
        
        AuthResponse createResponse = adminService.createUser(createRequest);
        assertTrue(createResponse.isSuccess());
        
        // Reset the spy to not count the creation event
        reset(eventPublisherService);
        
        // Now change the role
        ChangeRoleRequest changeRequest = new ChangeRoleRequest();
        changeRequest.setUsername("rolechangeuser");
        changeRequest.setRole(Role.ADMIN);

        // Act
        AuthResponse changeResponse = adminService.changeUserRole(changeRequest);

        // Assert
        assertTrue(changeResponse.isSuccess());
        
        ArgumentCaptor<UserRoleChangedEvent> eventCaptor = ArgumentCaptor.forClass(UserRoleChangedEvent.class);
        verify(eventPublisherService, times(1)).publishUserRoleChangedEvent(eventCaptor.capture());
        
        UserRoleChangedEvent capturedEvent = eventCaptor.getValue();
        assertNotNull(capturedEvent);
        assertEquals("rolechangeuser", capturedEvent.getUsername());
        assertEquals(Role.USER, capturedEvent.getPreviousRole());
        assertEquals(Role.ADMIN, capturedEvent.getNewRole());
        assertEquals("testsuperadmin", capturedEvent.getChangedBy());
        assertEquals("SUPER_ADMIN", capturedEvent.getChangedByRole());
        assertEquals("USER_ROLE_CHANGED", capturedEvent.getEventType());
        assertNotNull(capturedEvent.getUserId());
        assertNotNull(capturedEvent.getChangedAt());
    }

    @Test
    void testAdminCreatedByAdminPublishesAdminCreatedEvent() {
        // Arrange - authenticate as regular admin (not super admin)
        authenticateAsAdmin();
        
        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setUsername("regularuser456");
        request.setPassword("password123");
        request.setFirstName("Regular");
        request.setLastName("User");
        request.setEmail("regularuser456@example.com");
        request.setRole(Role.USER);

        // Act
        AuthResponse response = adminService.createUser(request);

        // Assert
        assertTrue(response.isSuccess());
        
        ArgumentCaptor<UserCreatedEvent> eventCaptor = ArgumentCaptor.forClass(UserCreatedEvent.class);
        verify(eventPublisherService, times(1)).publishUserCreatedEvent(eventCaptor.capture());
        
        UserCreatedEvent capturedEvent = eventCaptor.getValue();
        assertNotNull(capturedEvent);
        assertEquals("regularuser456", capturedEvent.getUsername());
        assertEquals(Role.USER, capturedEvent.getRole());
        assertEquals("testadmin", capturedEvent.getCreatedBy()); // Should be created by admin
        assertEquals("USER_CREATED", capturedEvent.getEventType());
    }

    @Test
    void testFailedUserCreationDoesNotPublishEvent() {
        // Arrange
        authenticateAsSuperAdmin();
        
        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setUsername("testsuperadmin"); // This username already exists
        request.setPassword("password123");
        request.setFirstName("Duplicate");
        request.setLastName("User");
        request.setEmail("duplicate@example.com");
        request.setRole(Role.USER);

        // Act
        AuthResponse response = adminService.createUser(request);

        // Assert
        assertFalse(response.isSuccess());
        
        // Verify no events were published
        verify(eventPublisherService, never()).publishUserCreatedEvent(any());
        verify(eventPublisherService, never()).publishAdminCreatedEvent(any());
    }

    @Test
    void testFailedRoleChangeDoesNotPublishEvent() {
        // Arrange
        authenticateAsAdmin(); // Regular admin cannot change roles
        
        ChangeRoleRequest request = new ChangeRoleRequest();
        request.setUsername("superadmin");
        request.setRole(Role.USER);

        // Act
        AuthResponse response = adminService.changeUserRole(request);

        // Assert
        assertFalse(response.isSuccess());
        
        // Verify no events were published
        verify(eventPublisherService, never()).publishUserRoleChangedEvent(any());
    }
}