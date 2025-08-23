package com.example.demo.event;

import com.example.demo.domain.Role;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AdminCreatedEventTest {

    @Test
    void testAdminCreatedEventCreation() {
        // Arrange
        Long adminId = 1L;
        String username = "testadmin";
        String email = "admin@example.com";
        String firstName = "Test";
        String lastName = "Admin";
        Role role = Role.ADMIN;
        String createdBy = "superadmin";
        String createdByRole = "SUPER_ADMIN";
        LocalDateTime createdAt = LocalDateTime.now();

        // Act
        AdminCreatedEvent event = new AdminCreatedEvent(
            adminId, username, email, firstName, lastName, role, createdBy, createdByRole, createdAt
        );

        // Assert
        assertEquals(adminId, event.getAdminId());
        assertEquals(username, event.getUsername());
        assertEquals(email, event.getEmail());
        assertEquals(firstName, event.getFirstName());
        assertEquals(lastName, event.getLastName());
        assertEquals(role, event.getRole());
        assertEquals(createdBy, event.getCreatedBy());
        assertEquals(createdByRole, event.getCreatedByRole());
        assertEquals(createdAt, event.getCreatedAt());
        assertEquals("ADMIN_CREATED", event.getEventType());
        assertNotNull(event.getEventId());
        assertNotNull(event.getTimestamp());
    }

    @Test
    void testDefaultConstructor() {
        // Act
        AdminCreatedEvent event = new AdminCreatedEvent();

        // Assert
        assertNull(event.getAdminId());
        assertNull(event.getUsername());
        assertNull(event.getEmail());
        assertNull(event.getFirstName());
        assertNull(event.getLastName());
        assertNull(event.getRole());
        assertNull(event.getCreatedBy());
        assertNull(event.getCreatedByRole());
        assertNull(event.getCreatedAt());
    }

    @Test
    void testSettersAndGetters() {
        // Arrange
        AdminCreatedEvent event = new AdminCreatedEvent();
        Long adminId = 2L;
        String username = "newadmin";
        String email = "newadmin@example.com";
        String firstName = "New";
        String lastName = "Admin";
        Role role = Role.ADMIN;
        String createdBy = "superadmin";
        String createdByRole = "SUPER_ADMIN";
        LocalDateTime createdAt = LocalDateTime.now().minusMinutes(15);

        // Act
        event.setAdminId(adminId);
        event.setUsername(username);
        event.setEmail(email);
        event.setFirstName(firstName);
        event.setLastName(lastName);
        event.setRole(role);
        event.setCreatedBy(createdBy);
        event.setCreatedByRole(createdByRole);
        event.setCreatedAt(createdAt);

        // Assert
        assertEquals(adminId, event.getAdminId());
        assertEquals(username, event.getUsername());
        assertEquals(email, event.getEmail());
        assertEquals(firstName, event.getFirstName());
        assertEquals(lastName, event.getLastName());
        assertEquals(role, event.getRole());
        assertEquals(createdBy, event.getCreatedBy());
        assertEquals(createdByRole, event.getCreatedByRole());
        assertEquals(createdAt, event.getCreatedAt());
    }

    @Test
    void testEqualsAndHashCode() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        AdminCreatedEvent event1 = new AdminCreatedEvent(
            1L, "admin1", "admin1@example.com", "Admin", "One", 
            Role.ADMIN, "superadmin", "SUPER_ADMIN", now
        );
        AdminCreatedEvent event2 = new AdminCreatedEvent(
            1L, "admin1", "admin1@example.com", "Admin", "One", 
            Role.ADMIN, "superadmin", "SUPER_ADMIN", now
        );
        AdminCreatedEvent event3 = new AdminCreatedEvent(
            2L, "admin2", "admin2@example.com", "Admin", "Two", 
            Role.ADMIN, "superadmin", "SUPER_ADMIN", now
        );

        // Act & Assert
        assertEquals(event1, event2);
        assertEquals(event1.hashCode(), event2.hashCode());
        assertNotEquals(event1, event3);
        assertNotEquals(event1.hashCode(), event3.hashCode());
    }

    @Test
    void testToString() {
        // Arrange
        AdminCreatedEvent event = new AdminCreatedEvent(
            1L, "testadmin", "admin@example.com", "Test", "Admin", 
            Role.ADMIN, "superadmin", "SUPER_ADMIN", LocalDateTime.now()
        );

        // Act
        String toString = event.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("testadmin"));
        assertTrue(toString.contains("admin@example.com"));
        assertTrue(toString.contains("ADMIN"));
        assertTrue(toString.contains("superadmin"));
        assertTrue(toString.contains("SUPER_ADMIN"));
    }

    @Test
    void testWithNullValues() {
        // Act
        AdminCreatedEvent event = new AdminCreatedEvent(
            null, null, null, null, null, null, null, null, null
        );

        // Assert
        assertNull(event.getAdminId());
        assertNull(event.getUsername());
        assertNull(event.getEmail());
        assertNull(event.getFirstName());
        assertNull(event.getLastName());
        assertNull(event.getRole());
        assertNull(event.getCreatedBy());
        assertNull(event.getCreatedByRole());
        assertNull(event.getCreatedAt());
        assertEquals("ADMIN_CREATED", event.getEventType());
    }

    @Test
    void testInheritanceFromBaseEvent() {
        // Arrange
        AdminCreatedEvent event = new AdminCreatedEvent(
            1L, "testadmin", "admin@example.com", "Test", "Admin", 
            Role.ADMIN, "superadmin", "SUPER_ADMIN", LocalDateTime.now()
        );

        // Assert
        assertTrue(event instanceof BaseEvent);
        assertEquals("user-management-service", event.getSource());
        assertEquals("1.0", event.getVersion());
        assertNotNull(event.getEventId());
        assertNotNull(event.getTimestamp());
    }
}