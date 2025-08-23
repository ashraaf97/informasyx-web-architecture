package com.example.demo.event;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BaseEventTest {

    private static class TestEvent extends BaseEvent {
        public TestEvent(String eventType) {
            super(eventType);
        }
    }

    @Test
    void testBaseEventCreation() {
        // Act
        TestEvent event = new TestEvent("TEST_EVENT");

        // Assert
        assertNotNull(event.getEventId());
        assertNotNull(event.getTimestamp());
        assertEquals("TEST_EVENT", event.getEventType());
        assertEquals("user-management-service", event.getSource());
        assertEquals("1.0", event.getVersion());
        assertTrue(event.getTimestamp().isBefore(LocalDateTime.now().plusSeconds(1)));
        assertTrue(event.getTimestamp().isAfter(LocalDateTime.now().minusSeconds(1)));
    }

    @Test
    void testDefaultConstructor() {
        // Act
        TestEvent event = new TestEvent("DEFAULT_TEST");

        // Assert
        assertNotNull(event.getEventId());
        assertNotNull(event.getTimestamp());
        assertEquals("DEFAULT_TEST", event.getEventType());
    }

    @Test
    void testEventIdIsUnique() {
        // Act
        TestEvent event1 = new TestEvent("TEST_1");
        TestEvent event2 = new TestEvent("TEST_2");

        // Assert
        assertNotEquals(event1.getEventId(), event2.getEventId());
    }

    @Test
    void testTimestampFormat() {
        // Act
        TestEvent event = new TestEvent("TIMESTAMP_TEST");

        // Assert
        assertNotNull(event.getTimestamp());
        // Verify timestamp is recent (within last second)
        LocalDateTime now = LocalDateTime.now();
        assertTrue(event.getTimestamp().isBefore(now.plusSeconds(1)));
        assertTrue(event.getTimestamp().isAfter(now.minusSeconds(1)));
    }

    @Test
    void testSettersAndGetters() {
        // Arrange
        TestEvent event = new TestEvent("SETTER_TEST");
        String customEventId = "custom-id";
        LocalDateTime customTimestamp = LocalDateTime.now().minusHours(1);
        String customSource = "custom-service";
        String customVersion = "2.0";

        // Act
        event.setEventId(customEventId);
        event.setTimestamp(customTimestamp);
        event.setSource(customSource);
        event.setVersion(customVersion);

        // Assert
        assertEquals(customEventId, event.getEventId());
        assertEquals(customTimestamp, event.getTimestamp());
        assertEquals(customSource, event.getSource());
        assertEquals(customVersion, event.getVersion());
    }
}