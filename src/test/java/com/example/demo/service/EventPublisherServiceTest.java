package com.example.demo.service;

import com.example.demo.domain.Role;
import com.example.demo.event.AdminCreatedEvent;
import com.example.demo.event.BaseEvent;
import com.example.demo.event.UserCreatedEvent;
import com.example.demo.event.UserRoleChangedEvent;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventPublisherServiceTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private SendResult<String, Object> sendResult;

    @Mock
    private RecordMetadata recordMetadata;

    private EventPublisherService eventPublisherService;

    @BeforeEach
    void setUp() {
        eventPublisherService = new EventPublisherService(kafkaTemplate);
        ReflectionTestUtils.setField(eventPublisherService, "userEventsTopicName", "user-events");
        ReflectionTestUtils.setField(eventPublisherService, "adminEventsTopicName", "admin-events");
    }

    @Test
    void testPublishUserCreatedEvent() {
        // Arrange
        UserCreatedEvent event = new UserCreatedEvent(
                1L, "testuser", "test@example.com", "Test", "User", 
                Role.USER, "admin", LocalDateTime.now()
        );
        
        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(mock(SendResult.class));
        when(kafkaTemplate.send(any(String.class), any(String.class), any(UserCreatedEvent.class)))
                .thenReturn(future);

        // Act
        eventPublisherService.publishUserCreatedEvent(event);

        // Assert
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<UserCreatedEvent> eventCaptor = ArgumentCaptor.forClass(UserCreatedEvent.class);
        
        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), eventCaptor.capture());
        
        assertEquals("user-events", topicCaptor.getValue());
        assertEquals("1", keyCaptor.getValue());
        assertEquals(event.getUserId(), eventCaptor.getValue().getUserId());
        assertEquals(event.getUsername(), eventCaptor.getValue().getUsername());
        assertEquals("USER_CREATED", eventCaptor.getValue().getEventType());
    }

    @Test
    void testPublishAdminCreatedEvent() {
        // Arrange
        AdminCreatedEvent event = new AdminCreatedEvent(
                2L, "testadmin", "admin@example.com", "Test", "Admin", 
                Role.ADMIN, "superadmin", "SUPER_ADMIN", LocalDateTime.now()
        );
        
        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(mock(SendResult.class));
        when(kafkaTemplate.send(any(String.class), any(String.class), any(AdminCreatedEvent.class)))
                .thenReturn(future);

        // Act
        eventPublisherService.publishAdminCreatedEvent(event);

        // Assert
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<AdminCreatedEvent> eventCaptor = ArgumentCaptor.forClass(AdminCreatedEvent.class);
        
        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), eventCaptor.capture());
        
        assertEquals("admin-events", topicCaptor.getValue());
        assertEquals("2", keyCaptor.getValue());
        assertEquals(event.getAdminId(), eventCaptor.getValue().getAdminId());
        assertEquals(event.getUsername(), eventCaptor.getValue().getUsername());
        assertEquals("ADMIN_CREATED", eventCaptor.getValue().getEventType());
    }

    @Test
    void testPublishUserRoleChangedEvent() {
        // Arrange
        UserRoleChangedEvent event = new UserRoleChangedEvent(
                3L, "testuser", Role.USER, Role.ADMIN, 
                "superadmin", "SUPER_ADMIN", LocalDateTime.now()
        );
        
        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(mock(SendResult.class));
        when(kafkaTemplate.send(any(String.class), any(String.class), any(UserRoleChangedEvent.class)))
                .thenReturn(future);

        // Act
        eventPublisherService.publishUserRoleChangedEvent(event);

        // Assert
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<UserRoleChangedEvent> eventCaptor = ArgumentCaptor.forClass(UserRoleChangedEvent.class);
        
        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), eventCaptor.capture());
        
        assertEquals("user-events", topicCaptor.getValue());
        assertEquals("3", keyCaptor.getValue());
        assertEquals(event.getUserId(), eventCaptor.getValue().getUserId());
        assertEquals(event.getUsername(), eventCaptor.getValue().getUsername());
        assertEquals("USER_ROLE_CHANGED", eventCaptor.getValue().getEventType());
        assertEquals(Role.USER, eventCaptor.getValue().getPreviousRole());
        assertEquals(Role.ADMIN, eventCaptor.getValue().getNewRole());
    }

    @Test
    void testPublishEventAsync() {
        // Arrange
        UserCreatedEvent event = new UserCreatedEvent(
                4L, "asyncuser", "async@example.com", "Async", "User", 
                Role.USER, "admin", LocalDateTime.now()
        );
        
        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(mock(SendResult.class));
        when(kafkaTemplate.send(any(String.class), any(String.class), any(UserCreatedEvent.class)))
                .thenReturn(future);

        // Act
        eventPublisherService.publishEventAsync("test-topic", "test-key", event);

        // Assert
        verify(kafkaTemplate).send(eq("test-topic"), eq("test-key"), eq(event));
    }

    @Test
    void testPublishUserCreatedEventFailure() {
        // Arrange
        UserCreatedEvent event = new UserCreatedEvent(
                1L, "testuser", "test@example.com", "Test", "User", 
                Role.USER, "admin", LocalDateTime.now()
        );
        
        when(kafkaTemplate.send(any(String.class), any(String.class), any(UserCreatedEvent.class)))
                .thenThrow(new RuntimeException("Kafka error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            eventPublisherService.publishUserCreatedEvent(event);
        });
        
        assertEquals("Failed to publish user created event", exception.getMessage());
    }

    @Test
    void testPublishAdminCreatedEventFailure() {
        // Arrange
        AdminCreatedEvent event = new AdminCreatedEvent(
                2L, "testadmin", "admin@example.com", "Test", "Admin", 
                Role.ADMIN, "superadmin", "SUPER_ADMIN", LocalDateTime.now()
        );
        
        when(kafkaTemplate.send(any(String.class), any(String.class), any(AdminCreatedEvent.class)))
                .thenThrow(new RuntimeException("Kafka error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            eventPublisherService.publishAdminCreatedEvent(event);
        });
        
        assertEquals("Failed to publish admin created event", exception.getMessage());
    }

    @Test
    void testPublishUserRoleChangedEventFailure() {
        // Arrange
        UserRoleChangedEvent event = new UserRoleChangedEvent(
                3L, "testuser", Role.USER, Role.ADMIN, 
                "superadmin", "SUPER_ADMIN", LocalDateTime.now()
        );
        
        when(kafkaTemplate.send(any(String.class), any(String.class), any(UserRoleChangedEvent.class)))
                .thenThrow(new RuntimeException("Kafka error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            eventPublisherService.publishUserRoleChangedEvent(event);
        });
        
        assertEquals("Failed to publish user role changed event", exception.getMessage());
    }

    @Test
    void testPublishEventAsyncSuccess() {
        // Arrange
        UserCreatedEvent event = new UserCreatedEvent(
                1L, "asyncuser", "async@example.com", "Async", "User", 
                Role.USER, "admin", LocalDateTime.now()
        );
        
        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(sendResult);
        when(sendResult.getRecordMetadata()).thenReturn(recordMetadata);
        when(recordMetadata.offset()).thenReturn(123L);
        when(kafkaTemplate.send(any(String.class), any(String.class), any(BaseEvent.class)))
                .thenReturn(future);

        // Act
        eventPublisherService.publishEventAsync("test-topic", "test-key", event);

        // Assert
        verify(kafkaTemplate).send("test-topic", "test-key", event);
    }

    @Test
    void testPublishEventAsyncFailure() {
        // Arrange
        UserCreatedEvent event = new UserCreatedEvent(
                1L, "asyncuser", "async@example.com", "Async", "User", 
                Role.USER, "admin", LocalDateTime.now()
        );
        
        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Async failure"));
        when(kafkaTemplate.send(any(String.class), any(String.class), any(BaseEvent.class)))
                .thenReturn(future);

        // Act
        assertDoesNotThrow(() -> eventPublisherService.publishEventAsync("test-topic", "test-key", event));

        // Assert
        verify(kafkaTemplate).send("test-topic", "test-key", event);
    }

    @Test
    void testPublishEventAsyncException() {
        // Arrange
        UserCreatedEvent event = new UserCreatedEvent(
                1L, "asyncuser", "async@example.com", "Async", "User", 
                Role.USER, "admin", LocalDateTime.now()
        );
        
        when(kafkaTemplate.send(any(String.class), any(String.class), any(BaseEvent.class)))
                .thenThrow(new RuntimeException("Template exception"));

        // Act & Assert - Should not throw exception
        assertDoesNotThrow(() -> eventPublisherService.publishEventAsync("test-topic", "test-key", event));
    }

    @Test
    void testPublishEventWithSuccessfulCompletion() {
        // Arrange
        UserCreatedEvent event = new UserCreatedEvent(
                1L, "testuser", "test@example.com", "Test", "User", 
                Role.USER, "admin", LocalDateTime.now()
        );
        
        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(sendResult);
        when(sendResult.getRecordMetadata()).thenReturn(recordMetadata);
        when(recordMetadata.offset()).thenReturn(456L);
        when(kafkaTemplate.send(any(String.class), any(String.class), any(UserCreatedEvent.class)))
                .thenReturn(future);

        // Act
        eventPublisherService.publishUserCreatedEvent(event);

        // Assert
        verify(kafkaTemplate).send(eq("user-events"), eq("1"), eq(event));
    }

    @Test
    void testPublishEventWithFailedCompletion() {
        // Arrange
        UserCreatedEvent event = new UserCreatedEvent(
                1L, "testuser", "test@example.com", "Test", "User", 
                Role.USER, "admin", LocalDateTime.now()
        );
        
        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Send failed"));
        when(kafkaTemplate.send(any(String.class), any(String.class), any(UserCreatedEvent.class)))
                .thenReturn(future);

        // Act & Assert - Should not throw exception in main thread
        assertDoesNotThrow(() -> eventPublisherService.publishUserCreatedEvent(event));
    }

    @Test
    void testPublishUserCreatedEventWithNullValues() {
        // Arrange
        UserCreatedEvent event = new UserCreatedEvent(
                null, null, null, null, null, null, null, null
        );
        

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            eventPublisherService.publishUserCreatedEvent(event);
        });
        
        assertEquals("Failed to publish user created event", exception.getMessage());
    }

    @Test
    void testPublishAdminCreatedEventWithCompleteData() {
        // Arrange
        AdminCreatedEvent event = new AdminCreatedEvent(
                5L, "fulladmin", "full@example.com", "Full", "Admin", 
                Role.ADMIN, "superadmin", "SUPER_ADMIN", LocalDateTime.now()
        );
        
        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(sendResult);
        when(kafkaTemplate.send(any(String.class), any(String.class), any(AdminCreatedEvent.class)))
                .thenReturn(future);

        // Act
        eventPublisherService.publishAdminCreatedEvent(event);

        // Assert
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<AdminCreatedEvent> eventCaptor = ArgumentCaptor.forClass(AdminCreatedEvent.class);
        
        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), eventCaptor.capture());
        
        assertEquals("admin-events", topicCaptor.getValue());
        assertEquals("5", keyCaptor.getValue());
        AdminCreatedEvent capturedEvent = eventCaptor.getValue();
        assertEquals("fulladmin", capturedEvent.getUsername());
        assertEquals("SUPER_ADMIN", capturedEvent.getCreatedByRole());
    }

    @Test
    void testPublishUserRoleChangedEventWithCompleteData() {
        // Arrange
        UserRoleChangedEvent event = new UserRoleChangedEvent(
                7L, "roleuser", Role.USER, Role.ADMIN, 
                "superadmin", "SUPER_ADMIN", LocalDateTime.now()
        );
        
        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(sendResult);
        when(kafkaTemplate.send(any(String.class), any(String.class), any(UserRoleChangedEvent.class)))
                .thenReturn(future);

        // Act
        eventPublisherService.publishUserRoleChangedEvent(event);

        // Assert
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<UserRoleChangedEvent> eventCaptor = ArgumentCaptor.forClass(UserRoleChangedEvent.class);
        
        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), eventCaptor.capture());
        
        assertEquals("user-events", topicCaptor.getValue());
        assertEquals("7", keyCaptor.getValue());
        UserRoleChangedEvent capturedEvent = eventCaptor.getValue();
        assertEquals("roleuser", capturedEvent.getUsername());
        assertEquals(Role.USER, capturedEvent.getPreviousRole());
        assertEquals(Role.ADMIN, capturedEvent.getNewRole());
        assertEquals("SUPER_ADMIN", capturedEvent.getChangedByRole());
    }

    @Test
    void testEventPublishingWithDifferentTopics() {
        // Arrange
        ReflectionTestUtils.setField(eventPublisherService, "userEventsTopicName", "custom-user-topic");
        ReflectionTestUtils.setField(eventPublisherService, "adminEventsTopicName", "custom-admin-topic");

        UserCreatedEvent userEvent = new UserCreatedEvent(
                1L, "user", "user@example.com", "User", "Name", 
                Role.USER, "admin", LocalDateTime.now()
        );
        AdminCreatedEvent adminEvent = new AdminCreatedEvent(
                2L, "admin", "admin@example.com", "Admin", "Name", 
                Role.ADMIN, "superadmin", "SUPER_ADMIN", LocalDateTime.now()
        );
        
        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(sendResult);
        when(kafkaTemplate.send(any(String.class), any(String.class), any(BaseEvent.class)))
                .thenReturn(future);

        // Act
        eventPublisherService.publishUserCreatedEvent(userEvent);
        eventPublisherService.publishAdminCreatedEvent(adminEvent);

        // Assert
        verify(kafkaTemplate).send("custom-user-topic", "1", userEvent);
        verify(kafkaTemplate).send("custom-admin-topic", "2", adminEvent);
    }
}