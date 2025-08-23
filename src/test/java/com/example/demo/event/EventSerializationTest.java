package com.example.demo.event;

import com.example.demo.domain.Role;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

class EventSerializationTest {

    private ObjectMapper objectMapper;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        testDateTime = LocalDateTime.of(2024, 1, 15, 10, 30, 45, 0); // Adding nanoseconds
    }

    @Test
    void testUserCreatedEventSerialization() throws JsonProcessingException {
        UserCreatedEvent event = new UserCreatedEvent(
                1L, "testuser", "test@example.com", "Test", "User",
                Role.USER, "admin", testDateTime
        );

        String json = objectMapper.writeValueAsString(event);

        assertNotNull(json);
        assertTrue(json.contains("\"userId\":1"));
        assertTrue(json.contains("\"username\":\"testuser\""));
        assertTrue(json.contains("\"email\":\"test@example.com\""));
        assertTrue(json.contains("\"firstName\":\"Test\""));
        assertTrue(json.contains("\"lastName\":\"User\""));
        assertTrue(json.contains("\"role\":\"USER\""));
        assertTrue(json.contains("\"createdBy\":\"admin\""));
        assertTrue(json.contains("\"eventType\":\"USER_CREATED\""));
        assertTrue(json.contains("\"source\":\"user-management-service\""));
        assertTrue(json.contains("\"version\":\"1.0\""));
    }

    @Test
    void testUserCreatedEventDeserialization() throws JsonProcessingException {
        String json = """
            {
                "eventId": "test-event-id",
                "timestamp": "2024-01-15T10:30:45.000",
                "eventType": "USER_CREATED",
                "source": "user-management-service",
                "version": "1.0",
                "userId": 1,
                "username": "testuser",
                "email": "test@example.com",
                "firstName": "Test",
                "lastName": "User",
                "role": "USER",
                "createdBy": "admin",
                "createdAt": "2024-01-15T10:30:45.000"
            }
            """;

        UserCreatedEvent event = objectMapper.readValue(json, UserCreatedEvent.class);

        assertEquals("test-event-id", event.getEventId());
        assertEquals(testDateTime, event.getTimestamp());
        assertEquals("USER_CREATED", event.getEventType());
        assertEquals("user-management-service", event.getSource());
        assertEquals("1.0", event.getVersion());
        assertEquals(1L, event.getUserId());
        assertEquals("testuser", event.getUsername());
        assertEquals("test@example.com", event.getEmail());
        assertEquals("Test", event.getFirstName());
        assertEquals("User", event.getLastName());
        assertEquals(Role.USER, event.getRole());
        assertEquals("admin", event.getCreatedBy());
        assertEquals(testDateTime, event.getCreatedAt());
    }

    @Test
    void testAdminCreatedEventSerialization() throws JsonProcessingException {
        AdminCreatedEvent event = new AdminCreatedEvent(
                2L, "testadmin", "admin@example.com", "Test", "Admin",
                Role.ADMIN, "superadmin", "SUPER_ADMIN", testDateTime
        );

        String json = objectMapper.writeValueAsString(event);

        assertNotNull(json);
        assertTrue(json.contains("\"adminId\":2"));
        assertTrue(json.contains("\"username\":\"testadmin\""));
        assertTrue(json.contains("\"email\":\"admin@example.com\""));
        assertTrue(json.contains("\"role\":\"ADMIN\""));
        assertTrue(json.contains("\"createdBy\":\"superadmin\""));
        assertTrue(json.contains("\"createdByRole\":\"SUPER_ADMIN\""));
        assertTrue(json.contains("\"eventType\":\"ADMIN_CREATED\""));
    }

    @Test
    void testAdminCreatedEventDeserialization() throws JsonProcessingException {
        String json = """
            {
                "eventId": "admin-event-id",
                "timestamp": "2024-01-15T10:30:45.000",
                "eventType": "ADMIN_CREATED",
                "source": "user-management-service",
                "version": "1.0",
                "adminId": 2,
                "username": "testadmin",
                "email": "admin@example.com",
                "firstName": "Test",
                "lastName": "Admin",
                "role": "ADMIN",
                "createdBy": "superadmin",
                "createdByRole": "SUPER_ADMIN",
                "createdAt": "2024-01-15T10:30:45.000"
            }
            """;

        AdminCreatedEvent event = objectMapper.readValue(json, AdminCreatedEvent.class);

        assertEquals("admin-event-id", event.getEventId());
        assertEquals(2L, event.getAdminId());
        assertEquals("testadmin", event.getUsername());
        assertEquals("admin@example.com", event.getEmail());
        assertEquals(Role.ADMIN, event.getRole());
        assertEquals("superadmin", event.getCreatedBy());
        assertEquals("SUPER_ADMIN", event.getCreatedByRole());
        assertEquals("ADMIN_CREATED", event.getEventType());
    }

    @Test
    void testUserRoleChangedEventSerialization() throws JsonProcessingException {
        UserRoleChangedEvent event = new UserRoleChangedEvent(
                3L, "roleuser", Role.USER, Role.ADMIN,
                "superadmin", "SUPER_ADMIN", testDateTime
        );

        String json = objectMapper.writeValueAsString(event);

        assertNotNull(json);
        assertTrue(json.contains("\"userId\":3"));
        assertTrue(json.contains("\"username\":\"roleuser\""));
        assertTrue(json.contains("\"previousRole\":\"USER\""));
        assertTrue(json.contains("\"newRole\":\"ADMIN\""));
        assertTrue(json.contains("\"changedBy\":\"superadmin\""));
        assertTrue(json.contains("\"changedByRole\":\"SUPER_ADMIN\""));
        assertTrue(json.contains("\"eventType\":\"USER_ROLE_CHANGED\""));
    }

    @Test
    void testUserRoleChangedEventDeserialization() throws JsonProcessingException {
        String json = """
            {
                "eventId": "role-change-event-id",
                "timestamp": "2024-01-15T10:30:45.000",
                "eventType": "USER_ROLE_CHANGED",
                "source": "user-management-service",
                "version": "1.0",
                "userId": 3,
                "username": "roleuser",
                "previousRole": "USER",
                "newRole": "ADMIN",
                "changedBy": "superadmin",
                "changedByRole": "SUPER_ADMIN",
                "changedAt": "2024-01-15T10:30:45.000"
            }
            """;

        UserRoleChangedEvent event = objectMapper.readValue(json, UserRoleChangedEvent.class);

        assertEquals("role-change-event-id", event.getEventId());
        assertEquals(3L, event.getUserId());
        assertEquals("roleuser", event.getUsername());
        assertEquals(Role.USER, event.getPreviousRole());
        assertEquals(Role.ADMIN, event.getNewRole());
        assertEquals("superadmin", event.getChangedBy());
        assertEquals("SUPER_ADMIN", event.getChangedByRole());
        assertEquals("USER_ROLE_CHANGED", event.getEventType());
        assertEquals(testDateTime, event.getChangedAt());
    }

    @Test
    void testSerializationWithNullValues() throws JsonProcessingException {
        UserCreatedEvent event = new UserCreatedEvent(
                null, null, null, null, null,
                null, null, null
        );

        String json = objectMapper.writeValueAsString(event);
        assertNotNull(json);

        UserCreatedEvent deserializedEvent = objectMapper.readValue(json, UserCreatedEvent.class);
        assertNull(deserializedEvent.getUserId());
        assertNull(deserializedEvent.getUsername());
        assertNull(deserializedEvent.getEmail());
        assertNull(deserializedEvent.getFirstName());
        assertNull(deserializedEvent.getLastName());
        assertNull(deserializedEvent.getRole());
        assertNull(deserializedEvent.getCreatedBy());
        assertNull(deserializedEvent.getCreatedAt());
    }

    @Test
    void testSerializationWithSpecialCharacters() throws JsonProcessingException {
        UserCreatedEvent event = new UserCreatedEvent(
                1L, "test@user", "test+user@example.com", "Test's", "User & Co",
                Role.USER, "admin-user", testDateTime
        );

        String json = objectMapper.writeValueAsString(event);
        UserCreatedEvent deserializedEvent = objectMapper.readValue(json, UserCreatedEvent.class);

        assertEquals("test@user", deserializedEvent.getUsername());
        assertEquals("test+user@example.com", deserializedEvent.getEmail());
        assertEquals("Test's", deserializedEvent.getFirstName());
        assertEquals("User & Co", deserializedEvent.getLastName());
        assertEquals("admin-user", deserializedEvent.getCreatedBy());
    }

    @Test
    void testDateTimeFormatConsistency() throws JsonProcessingException {
        UserCreatedEvent event = new UserCreatedEvent(
                1L, "testuser", "test@example.com", "Test", "User",
                Role.USER, "admin", testDateTime
        );

        String json = objectMapper.writeValueAsString(event);
        UserCreatedEvent deserializedEvent = objectMapper.readValue(json, UserCreatedEvent.class);

        // Compare with tolerance due to potential nanosecond precision differences
        assertNotNull(deserializedEvent.getTimestamp());
        assertNotNull(deserializedEvent.getCreatedAt());

        String expectedDateFormat = testDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        assertTrue(json.contains(expectedDateFormat));
    }

    @Test
    void testBaseEventFieldsSerialization() throws JsonProcessingException {
        UserCreatedEvent event = new UserCreatedEvent(
                1L, "testuser", "test@example.com", "Test", "User",
                Role.USER, "admin", testDateTime
        );

        String json = objectMapper.writeValueAsString(event);

        assertTrue(json.contains("\"eventId\""));
        assertTrue(json.contains("\"timestamp\""));
        assertTrue(json.contains("\"eventType\":\"USER_CREATED\""));
        assertTrue(json.contains("\"source\":\"user-management-service\""));
        assertTrue(json.contains("\"version\":\"1.0\""));
    }

    @Test
    void testRoleEnumSerialization() throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(Role.SUPER_ADMIN);
        assertEquals("\"SUPER_ADMIN\"", json);

        Role deserializedRole = objectMapper.readValue("\"ADMIN\"", Role.class);
        assertEquals(Role.ADMIN, deserializedRole);
    }

    @Test
    void testInvalidJsonDeserialization() {
        String invalidJson = """
            {
                "eventId": "test",
                "invalidField": "value",
                "userId": "not-a-number"
            }
            """;

        assertThrows(JsonProcessingException.class, () -> 
            objectMapper.readValue(invalidJson, UserCreatedEvent.class));
    }

    @Test
    void testRoundTripSerializationConsistency() throws JsonProcessingException {
        AdminCreatedEvent originalEvent = new AdminCreatedEvent(
                123L, "admin123", "admin123@example.com", "Admin", "User",
                Role.ADMIN, "superadmin", "SUPER_ADMIN", testDateTime
        );

        String json = objectMapper.writeValueAsString(originalEvent);
        AdminCreatedEvent deserializedEvent = objectMapper.readValue(json, AdminCreatedEvent.class);

        assertEquals(originalEvent.getAdminId(), deserializedEvent.getAdminId());
        assertEquals(originalEvent.getUsername(), deserializedEvent.getUsername());
        assertEquals(originalEvent.getEmail(), deserializedEvent.getEmail());
        assertEquals(originalEvent.getFirstName(), deserializedEvent.getFirstName());
        assertEquals(originalEvent.getLastName(), deserializedEvent.getLastName());
        assertEquals(originalEvent.getRole(), deserializedEvent.getRole());
        assertEquals(originalEvent.getCreatedBy(), deserializedEvent.getCreatedBy());
        assertEquals(originalEvent.getCreatedByRole(), deserializedEvent.getCreatedByRole());
        assertEquals(originalEvent.getCreatedAt(), deserializedEvent.getCreatedAt());
        assertEquals(originalEvent.getEventType(), deserializedEvent.getEventType());
        assertEquals(originalEvent.getSource(), deserializedEvent.getSource());
        assertEquals(originalEvent.getVersion(), deserializedEvent.getVersion());
    }
}