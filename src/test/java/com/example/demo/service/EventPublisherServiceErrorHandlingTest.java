package com.example.demo.service;

import com.example.demo.domain.Role;
import com.example.demo.event.UserCreatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventPublisherServiceErrorHandlingTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private CompletableFuture<SendResult<String, Object>> mockFuture;

    @InjectMocks
    private EventPublisherService eventPublisherService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(eventPublisherService, "userEventsTopicName", "user-events");
        ReflectionTestUtils.setField(eventPublisherService, "adminEventsTopicName", "admin-events");
    }

    @Test
    void testPublishEventWithKafkaTemplateException() {
        UserCreatedEvent event = new UserCreatedEvent(
                1L, "testuser", "test@example.com", "Test", "User",
                Role.USER, "admin", LocalDateTime.now()
        );

        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenThrow(new RuntimeException("Kafka connection failed"));

        assertThrows(RuntimeException.class, () -> 
            eventPublisherService.publishUserCreatedEvent(event));

        verify(kafkaTemplate, times(1)).send(eq("user-events"), eq("1"), eq(event));
    }

    @Test
    void testPublishEventAsyncWithExecutionException() throws Exception {
        UserCreatedEvent event = new UserCreatedEvent(
                1L, "testuser", "test@example.com", "Test", "User",
                Role.USER, "admin", LocalDateTime.now()
        );

        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(mockFuture);

        assertDoesNotThrow(() -> eventPublisherService.publishEventAsync("user-events", "1", event));
        verify(kafkaTemplate, times(1)).send("user-events", "1", event);
    }

    @Test
    void testPublishEventWithNullEvent() {
        assertThrows(RuntimeException.class, () -> 
            eventPublisherService.publishUserCreatedEvent(null));
    }

    @Test
    void testPublishEventWithNullUserId() {
        UserCreatedEvent event = new UserCreatedEvent(
                null, "testuser", "test@example.com", "Test", "User",
                Role.USER, "admin", LocalDateTime.now()
        );

        assertThrows(RuntimeException.class, () -> 
            eventPublisherService.publishUserCreatedEvent(event));
    }

    @Test
    void testPublishEventWithEmptyTopicName() {
        ReflectionTestUtils.setField(eventPublisherService, "userEventsTopicName", "");

        UserCreatedEvent event = new UserCreatedEvent(
                1L, "testuser", "test@example.com", "Test", "User",
                Role.USER, "admin", LocalDateTime.now()
        );

        assertThrows(RuntimeException.class, () -> 
            eventPublisherService.publishUserCreatedEvent(event));
    }

    @Test
    void testPublishEventWithNullTopicName() {
        ReflectionTestUtils.setField(eventPublisherService, "userEventsTopicName", null);

        UserCreatedEvent event = new UserCreatedEvent(
                1L, "testuser", "test@example.com", "Test", "User",
                Role.USER, "admin", LocalDateTime.now()
        );

        assertThrows(RuntimeException.class, () -> 
            eventPublisherService.publishUserCreatedEvent(event));
    }

    @Test
    void testPublishAsyncEventWithNullParameters() {
        UserCreatedEvent validEvent = new UserCreatedEvent(
                1L, "testuser", "test@example.com", "Test", "User",
                Role.USER, "admin", LocalDateTime.now()
        );
        
        assertThrows(RuntimeException.class, () -> 
            eventPublisherService.publishEventAsync(null, "1", validEvent));

        assertThrows(RuntimeException.class, () -> 
            eventPublisherService.publishEventAsync("topic", null, validEvent));

        assertThrows(RuntimeException.class, () -> 
            eventPublisherService.publishEventAsync("topic", "1", null));
    }

    @Test
    void testPublishEventWithInterruptedException() throws Exception {
        UserCreatedEvent event = new UserCreatedEvent(
                1L, "testuser", "test@example.com", "Test", "User",
                Role.USER, "admin", LocalDateTime.now()
        );

        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(mockFuture);

        assertDoesNotThrow(() -> eventPublisherService.publishEventAsync("user-events", "1", event));
    }

    @Test
    void testPublishEventWithSerializationError() {
        UserCreatedEvent event = new UserCreatedEvent(
                1L, "testuser", "test@example.com", "Test", "User",
                Role.USER, "admin", LocalDateTime.now()
        );

        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenThrow(new RuntimeException("Serialization failed"));

        assertThrows(RuntimeException.class, () -> 
            eventPublisherService.publishUserCreatedEvent(event));
    }

    @Test
    void testPublishEventWithTimeoutException() throws Exception {
        UserCreatedEvent event = new UserCreatedEvent(
                1L, "testuser", "test@example.com", "Test", "User",
                Role.USER, "admin", LocalDateTime.now()
        );

        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(mockFuture);

        assertDoesNotThrow(() -> eventPublisherService.publishEventAsync("user-events", "1", event));
    }

    @Test
    void testMultipleFailedPublishAttempts() {
        UserCreatedEvent event = new UserCreatedEvent(
                1L, "testuser", "test@example.com", "Test", "User",
                Role.USER, "admin", LocalDateTime.now()
        );

        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenThrow(new RuntimeException("First failure"))
                .thenThrow(new RuntimeException("Second failure"))
                .thenThrow(new RuntimeException("Third failure"));

        assertThrows(RuntimeException.class, () -> 
            eventPublisherService.publishUserCreatedEvent(event));
        assertThrows(RuntimeException.class, () -> 
            eventPublisherService.publishUserCreatedEvent(event));
        assertThrows(RuntimeException.class, () -> 
            eventPublisherService.publishUserCreatedEvent(event));

        verify(kafkaTemplate, times(3)).send(anyString(), anyString(), any());
    }

    @Test
    void testErrorRecoveryAfterFailure() {
        UserCreatedEvent event = new UserCreatedEvent(
                1L, "testuser", "test@example.com", "Test", "User",
                Role.USER, "admin", LocalDateTime.now()
        );

        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenThrow(new RuntimeException("Temporary failure"))
                .thenReturn(mockFuture);

        assertThrows(RuntimeException.class, () -> 
            eventPublisherService.publishUserCreatedEvent(event));

        assertDoesNotThrow(() -> 
            eventPublisherService.publishUserCreatedEvent(event));

        verify(kafkaTemplate, times(2)).send(anyString(), anyString(), any());
    }
}