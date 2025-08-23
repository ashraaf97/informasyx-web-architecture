package com.example.demo.event;

import com.example.demo.domain.Role;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserCreatedEventTest {

    @Test
    void testUserCreatedEventCreation() {
        // Arrange
        Long userId = 1L;
        String username = "testuser";
        String email = "test@example.com";
        String firstName = "Test";
        String lastName = "User";
        Role role = Role.USER;
        String createdBy = "admin";
        LocalDateTime createdAt = LocalDateTime.now();

        // Act
        UserCreatedEvent event = new UserCreatedEvent(
            userId, username, email, firstName, lastName, role, createdBy, createdAt
        );

        // Assert
        assertEquals(userId, event.getUserId());
        assertEquals(username, event.getUsername());
        assertEquals(email, event.getEmail());
        assertEquals(firstName, event.getFirstName());
        assertEquals(lastName, event.getLastName());
        assertEquals(role, event.getRole());
        assertEquals(createdBy, event.getCreatedBy());
        assertEquals(createdAt, event.getCreatedAt());
        assertEquals("USER_CREATED", event.getEventType());
        assertNotNull(event.getEventId());
        assertNotNull(event.getTimestamp());
    }

    @Test
    void testDefaultConstructor() {
        // Act
        UserCreatedEvent event = new UserCreatedEvent();

        // Assert
        assertNull(event.getUserId());
        assertNull(event.getUsername());
        assertNull(event.getEmail());
        assertNull(event.getFirstName());
        assertNull(event.getLastName());
        assertNull(event.getRole());
        assertNull(event.getCreatedBy());
        assertNull(event.getCreatedAt());
    }

    @Test
    void testSettersAndGetters() {
        // Arrange
        UserCreatedEvent event = new UserCreatedEvent();
        Long userId = 2L;
        String username = "newuser";
        String email = "newuser@example.com";
        String firstName = "New";
        String lastName = "User";
        Role role = Role.ADMIN;
        String createdBy = "superadmin";
        LocalDateTime createdAt = LocalDateTime.now().minusMinutes(30);

        // Act
        event.setUserId(userId);
        event.setUsername(username);
        event.setEmail(email);
        event.setFirstName(firstName);
        event.setLastName(lastName);
        event.setRole(role);
        event.setCreatedBy(createdBy);
        event.setCreatedAt(createdAt);

        // Assert
        assertEquals(userId, event.getUserId());
        assertEquals(username, event.getUsername());
        assertEquals(email, event.getEmail());
        assertEquals(firstName, event.getFirstName());
        assertEquals(lastName, event.getLastName());
        assertEquals(role, event.getRole());
        assertEquals(createdBy, event.getCreatedBy());
        assertEquals(createdAt, event.getCreatedAt());
    }

    @Test
    void testEqualsAndHashCode() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        UserCreatedEvent event1 = new UserCreatedEvent(
            1L, "user1", "user1@example.com", "User", "One", Role.USER, "admin", now
        );
        UserCreatedEvent event2 = new UserCreatedEvent(
            1L, "user1", "user1@example.com", "User", "One", Role.USER, "admin", now
        );
        UserCreatedEvent event3 = new UserCreatedEvent(
            2L, "user2", "user2@example.com", "User", "Two", Role.ADMIN, "superadmin", now
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
        UserCreatedEvent event = new UserCreatedEvent(
            1L, "testuser", "test@example.com", "Test", "User", 
            Role.USER, "admin", LocalDateTime.now()
        );

        // Act
        String toString = event.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("testuser"));
        assertTrue(toString.contains("test@example.com"));
        assertTrue(toString.contains("USER"));
        assertTrue(toString.contains("admin"));
    }

    @Test
    void testWithNullValues() {
        // Act
        UserCreatedEvent event = new UserCreatedEvent(
            null, null, null, null, null, null, null, null
        );

        // Assert
        assertNull(event.getUserId());
        assertNull(event.getUsername());
        assertNull(event.getEmail());
        assertNull(event.getFirstName());
        assertNull(event.getLastName());
        assertNull(event.getRole());
        assertNull(event.getCreatedBy());
        assertNull(event.getCreatedAt());
        assertEquals("USER_CREATED", event.getEventType());
    }
}