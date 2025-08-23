package com.example.demo.event;

import com.example.demo.domain.Role;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserRoleChangedEventTest {

    @Test
    void testUserRoleChangedEventCreation() {
        // Arrange
        Long userId = 1L;
        String username = "testuser";
        Role previousRole = Role.USER;
        Role newRole = Role.ADMIN;
        String changedBy = "superadmin";
        String changedByRole = "SUPER_ADMIN";
        LocalDateTime changedAt = LocalDateTime.now();

        // Act
        UserRoleChangedEvent event = new UserRoleChangedEvent(
            userId, username, previousRole, newRole, changedBy, changedByRole, changedAt
        );

        // Assert
        assertEquals(userId, event.getUserId());
        assertEquals(username, event.getUsername());
        assertEquals(previousRole, event.getPreviousRole());
        assertEquals(newRole, event.getNewRole());
        assertEquals(changedBy, event.getChangedBy());
        assertEquals(changedByRole, event.getChangedByRole());
        assertEquals(changedAt, event.getChangedAt());
        assertEquals("USER_ROLE_CHANGED", event.getEventType());
        assertNotNull(event.getEventId());
        assertNotNull(event.getTimestamp());
    }

    @Test
    void testDefaultConstructor() {
        // Act
        UserRoleChangedEvent event = new UserRoleChangedEvent();

        // Assert
        assertNull(event.getUserId());
        assertNull(event.getUsername());
        assertNull(event.getPreviousRole());
        assertNull(event.getNewRole());
        assertNull(event.getChangedBy());
        assertNull(event.getChangedByRole());
        assertNull(event.getChangedAt());
    }

    @Test
    void testSettersAndGetters() {
        // Arrange
        UserRoleChangedEvent event = new UserRoleChangedEvent();
        Long userId = 2L;
        String username = "changeuser";
        Role previousRole = Role.ADMIN;
        Role newRole = Role.USER;
        String changedBy = "superadmin";
        String changedByRole = "SUPER_ADMIN";
        LocalDateTime changedAt = LocalDateTime.now().minusMinutes(10);

        // Act
        event.setUserId(userId);
        event.setUsername(username);
        event.setPreviousRole(previousRole);
        event.setNewRole(newRole);
        event.setChangedBy(changedBy);
        event.setChangedByRole(changedByRole);
        event.setChangedAt(changedAt);

        // Assert
        assertEquals(userId, event.getUserId());
        assertEquals(username, event.getUsername());
        assertEquals(previousRole, event.getPreviousRole());
        assertEquals(newRole, event.getNewRole());
        assertEquals(changedBy, event.getChangedBy());
        assertEquals(changedByRole, event.getChangedByRole());
        assertEquals(changedAt, event.getChangedAt());
    }

    @Test
    void testRoleUpgrade() {
        // Arrange & Act
        UserRoleChangedEvent event = new UserRoleChangedEvent(
            1L, "upgradeuser", Role.USER, Role.ADMIN, 
            "superadmin", "SUPER_ADMIN", LocalDateTime.now()
        );

        // Assert
        assertEquals(Role.USER, event.getPreviousRole());
        assertEquals(Role.ADMIN, event.getNewRole());
        assertEquals("USER_ROLE_CHANGED", event.getEventType());
    }

    @Test
    void testRoleDowngrade() {
        // Arrange & Act
        UserRoleChangedEvent event = new UserRoleChangedEvent(
            1L, "downgradeuser", Role.ADMIN, Role.USER, 
            "superadmin", "SUPER_ADMIN", LocalDateTime.now()
        );

        // Assert
        assertEquals(Role.ADMIN, event.getPreviousRole());
        assertEquals(Role.USER, event.getNewRole());
        assertEquals("USER_ROLE_CHANGED", event.getEventType());
    }

    @Test
    void testEqualsAndHashCode() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        UserRoleChangedEvent event1 = new UserRoleChangedEvent(
            1L, "user1", Role.USER, Role.ADMIN, "superadmin", "SUPER_ADMIN", now
        );
        UserRoleChangedEvent event2 = new UserRoleChangedEvent(
            1L, "user1", Role.USER, Role.ADMIN, "superadmin", "SUPER_ADMIN", now
        );
        UserRoleChangedEvent event3 = new UserRoleChangedEvent(
            2L, "user2", Role.ADMIN, Role.USER, "superadmin", "SUPER_ADMIN", now
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
        UserRoleChangedEvent event = new UserRoleChangedEvent(
            1L, "testuser", Role.USER, Role.ADMIN, 
            "superadmin", "SUPER_ADMIN", LocalDateTime.now()
        );

        // Act
        String toString = event.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("testuser"));
        assertTrue(toString.contains("USER"));
        assertTrue(toString.contains("ADMIN"));
        assertTrue(toString.contains("superadmin"));
        assertTrue(toString.contains("SUPER_ADMIN"));
    }

    @Test
    void testWithNullValues() {
        // Act
        UserRoleChangedEvent event = new UserRoleChangedEvent(
            null, null, null, null, null, null, null
        );

        // Assert
        assertNull(event.getUserId());
        assertNull(event.getUsername());
        assertNull(event.getPreviousRole());
        assertNull(event.getNewRole());
        assertNull(event.getChangedBy());
        assertNull(event.getChangedByRole());
        assertNull(event.getChangedAt());
        assertEquals("USER_ROLE_CHANGED", event.getEventType());
    }

    @Test
    void testInheritanceFromBaseEvent() {
        // Arrange
        UserRoleChangedEvent event = new UserRoleChangedEvent(
            1L, "testuser", Role.USER, Role.ADMIN, 
            "superadmin", "SUPER_ADMIN", LocalDateTime.now()
        );

        // Assert
        assertInstanceOf(BaseEvent.class, event);
        assertEquals("user-management-service", event.getSource());
        assertEquals("1.0", event.getVersion());
        assertNotNull(event.getEventId());
        assertNotNull(event.getTimestamp());
    }

    @Test
    void testSameRoleChange() {
        // Arrange & Act - Testing edge case where roles are the same
        UserRoleChangedEvent event = new UserRoleChangedEvent(
            1L, "testuser", Role.USER, Role.USER, 
            "superadmin", "SUPER_ADMIN", LocalDateTime.now()
        );

        // Assert
        assertEquals(Role.USER, event.getPreviousRole());
        assertEquals(Role.USER, event.getNewRole());
        assertEquals("USER_ROLE_CHANGED", event.getEventType());
    }
}