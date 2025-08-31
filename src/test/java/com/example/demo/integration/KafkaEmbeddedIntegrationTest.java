package com.example.demo.integration;

import com.example.demo.domain.Role;
import com.example.demo.event.AdminCreatedEvent;
import com.example.demo.event.BaseEvent;
import com.example.demo.event.UserCreatedEvent;
import com.example.demo.event.UserRoleChangedEvent;
import com.example.demo.service.EventPublisherService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@EmbeddedKafka(
    partitions = 1,
    topics = {"test-user-events", "test-admin-events"},
    brokerProperties = {
        "listeners=PLAINTEXT://localhost:9092",
        "port=9092"
    }
)
@TestPropertySource(locations = "classpath:application-integration.properties", properties = {
    "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
    "app.kafka.topics.user-events=test-user-events",
    "app.kafka.topics.admin-events=test-admin-events"
})
@DirtiesContext
class KafkaEmbeddedIntegrationTest {

    @Autowired
    private EventPublisherService eventPublisherService;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    private Consumer<String, Object> userEventsConsumer;
    private Consumer<String, Object> adminEventsConsumer;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Setup consumer for user events
        Map<String, Object> userConsumerProps = KafkaTestUtils.consumerProps(
            "user-test-group", "true", embeddedKafka);
        userConsumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        userConsumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        userConsumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "com.example.demo.event");
        userConsumerProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, Object.class);

        ConsumerFactory<String, Object> userConsumerFactory = new DefaultKafkaConsumerFactory<>(userConsumerProps);
        userEventsConsumer = userConsumerFactory.createConsumer();
        userEventsConsumer.subscribe(Collections.singletonList("test-user-events"));

        // Setup consumer for admin events
        Map<String, Object> adminConsumerProps = KafkaTestUtils.consumerProps(
            "admin-test-group", "true", embeddedKafka);
        adminConsumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        adminConsumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        adminConsumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "com.example.demo.event");
        adminConsumerProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, Object.class);

        ConsumerFactory<String, Object> adminConsumerFactory = new DefaultKafkaConsumerFactory<>(adminConsumerProps);
        adminEventsConsumer = adminConsumerFactory.createConsumer();
        adminEventsConsumer.subscribe(Collections.singletonList("test-admin-events"));
    }

    @AfterEach
    void tearDown() {
        if (userEventsConsumer != null) {
            userEventsConsumer.close();
        }
        if (adminEventsConsumer != null) {
            adminEventsConsumer.close();
        }
    }

    @Test
    void testUserCreatedEventIsPublishedAndConsumed() throws Exception {
        // Arrange
        UserCreatedEvent event = new UserCreatedEvent(
                1L, "testuser", "test@example.com", "Test", "User",
                Role.USER, "admin", LocalDateTime.now()
        );

        // Act
        eventPublisherService.publishUserCreatedEvent(event);

        // Assert
        ConsumerRecords<String, Object> records = userEventsConsumer.poll(Duration.ofSeconds(10));
        assertFalse(records.isEmpty());

        ConsumerRecord<String, Object> record = records.iterator().next();
        assertEquals("1", record.key());
        assertNotNull(record.value());

        // Convert the received object to JSON and back to verify structure
        String jsonValue = objectMapper.writeValueAsString(record.value());
        UserCreatedEvent receivedEvent = objectMapper.readValue(jsonValue, UserCreatedEvent.class);

        assertEquals(event.getUserId(), receivedEvent.getUserId());
        assertEquals(event.getUsername(), receivedEvent.getUsername());
        assertEquals(event.getEmail(), receivedEvent.getEmail());
        assertEquals(event.getFirstName(), receivedEvent.getFirstName());
        assertEquals(event.getLastName(), receivedEvent.getLastName());
        assertEquals(event.getRole(), receivedEvent.getRole());
        assertEquals(event.getCreatedBy(), receivedEvent.getCreatedBy());
        assertEquals("USER_CREATED", receivedEvent.getEventType());
    }

    @Test
    void testAdminCreatedEventIsPublishedAndConsumed() throws Exception {
        // Arrange
        AdminCreatedEvent event = new AdminCreatedEvent(
                2L, "testadmin", "admin@example.com", "Test", "Admin",
                Role.ADMIN, "superadmin", "SUPER_ADMIN", LocalDateTime.now()
        );

        // Act
        eventPublisherService.publishAdminCreatedEvent(event);

        // Assert
        ConsumerRecords<String, Object> records = adminEventsConsumer.poll(Duration.ofSeconds(10));
        assertFalse(records.isEmpty());

        ConsumerRecord<String, Object> record = records.iterator().next();
        assertEquals("2", record.key());
        assertNotNull(record.value());

        // Convert the received object to JSON and back to verify structure
        String jsonValue = objectMapper.writeValueAsString(record.value());
        AdminCreatedEvent receivedEvent = objectMapper.readValue(jsonValue, AdminCreatedEvent.class);

        assertEquals(event.getAdminId(), receivedEvent.getAdminId());
        assertEquals(event.getUsername(), receivedEvent.getUsername());
        assertEquals(event.getEmail(), receivedEvent.getEmail());
        assertEquals(event.getFirstName(), receivedEvent.getFirstName());
        assertEquals(event.getLastName(), receivedEvent.getLastName());
        assertEquals(event.getRole(), receivedEvent.getRole());
        assertEquals(event.getCreatedBy(), receivedEvent.getCreatedBy());
        assertEquals(event.getCreatedByRole(), receivedEvent.getCreatedByRole());
        assertEquals("ADMIN_CREATED", receivedEvent.getEventType());
    }

    @Test
    void testUserRoleChangedEventIsPublishedAndConsumed() throws Exception {
        // Arrange
        UserRoleChangedEvent event = new UserRoleChangedEvent(
                3L, "roleuser", Role.USER, Role.ADMIN,
                "superadmin", "SUPER_ADMIN", LocalDateTime.now()
        );

        // Act
        eventPublisherService.publishUserRoleChangedEvent(event);

        // Assert
        ConsumerRecords<String, Object> records = userEventsConsumer.poll(Duration.ofSeconds(10));
        assertFalse(records.isEmpty());

        ConsumerRecord<String, Object> record = records.iterator().next();
        assertEquals("3", record.key());
        assertNotNull(record.value());

        // Convert the received object to JSON and back to verify structure
        String jsonValue = objectMapper.writeValueAsString(record.value());
        UserRoleChangedEvent receivedEvent = objectMapper.readValue(jsonValue, UserRoleChangedEvent.class);

        assertEquals(event.getUserId(), receivedEvent.getUserId());
        assertEquals(event.getUsername(), receivedEvent.getUsername());
        assertEquals(event.getPreviousRole(), receivedEvent.getPreviousRole());
        assertEquals(event.getNewRole(), receivedEvent.getNewRole());
        assertEquals(event.getChangedBy(), receivedEvent.getChangedBy());
        assertEquals(event.getChangedByRole(), receivedEvent.getChangedByRole());
        assertEquals("USER_ROLE_CHANGED", receivedEvent.getEventType());
    }

    @Test
    void testMultipleEventsArePublishedCorrectly() throws Exception {
        // Arrange
        UserCreatedEvent userEvent = new UserCreatedEvent(
                4L, "multiuser", "multi@example.com", "Multi", "User",
                Role.USER, "admin", LocalDateTime.now()
        );

        AdminCreatedEvent adminEvent = new AdminCreatedEvent(
                5L, "multiadmin", "multiadmin@example.com", "Multi", "Admin",
                Role.ADMIN, "superadmin", "SUPER_ADMIN", LocalDateTime.now()
        );

        // Act
        eventPublisherService.publishUserCreatedEvent(userEvent);
        eventPublisherService.publishAdminCreatedEvent(adminEvent);

        // Assert user event
        ConsumerRecords<String, Object> userRecords = userEventsConsumer.poll(Duration.ofSeconds(10));
        assertFalse(userRecords.isEmpty());
        assertEquals(1, userRecords.count());

        ConsumerRecord<String, Object> userRecord = userRecords.iterator().next();
        assertEquals("4", userRecord.key());

        // Assert admin event
        ConsumerRecords<String, Object> adminRecords = adminEventsConsumer.poll(Duration.ofSeconds(10));
        assertFalse(adminRecords.isEmpty());
        assertEquals(1, adminRecords.count());

        ConsumerRecord<String, Object> adminRecord = adminRecords.iterator().next();
        assertEquals("5", adminRecord.key());
    }

    @Test
    void testEventMetadataIsCorrect() throws Exception {
        // Arrange
        UserCreatedEvent event = new UserCreatedEvent(
                6L, "metauser", "meta@example.com", "Meta", "User",
                Role.USER, "admin", LocalDateTime.now()
        );

        // Act
        eventPublisherService.publishUserCreatedEvent(event);

        // Assert
        ConsumerRecords<String, Object> records = userEventsConsumer.poll(Duration.ofSeconds(10));
        assertFalse(records.isEmpty());

        ConsumerRecord<String, Object> record = records.iterator().next();
        
        // Verify topic
        assertEquals("test-user-events", record.topic());
        
        // Verify partition (should be 0 for single partition)
        assertEquals(0, record.partition());
        
        // Verify key
        assertEquals("6", record.key());
        
        // Verify event has proper metadata
        String jsonValue = objectMapper.writeValueAsString(record.value());
        UserCreatedEvent receivedEvent = objectMapper.readValue(jsonValue, UserCreatedEvent.class);
        
        assertNotNull(receivedEvent.getEventId());
        assertNotNull(receivedEvent.getTimestamp());
        assertEquals("user-management-service", receivedEvent.getSource());
        assertEquals("1.0", receivedEvent.getVersion());
        assertEquals("USER_CREATED", receivedEvent.getEventType());
    }

    @Test
    void testEventSerializationAndDeserialization() throws Exception {
        // Test all event types can be serialized and deserialized properly
        LocalDateTime testTime = LocalDateTime.now();

        // Test UserCreatedEvent
        UserCreatedEvent userEvent = new UserCreatedEvent(
                7L, "serializeuser", "serialize@example.com", "Serialize", "User",
                Role.USER, "admin", testTime
        );

        eventPublisherService.publishUserCreatedEvent(userEvent);

        ConsumerRecords<String, Object> userRecords = userEventsConsumer.poll(Duration.ofSeconds(10));
        assertFalse(userRecords.isEmpty());

        ConsumerRecord<String, Object> userRecord = userRecords.iterator().next();
        String userJson = objectMapper.writeValueAsString(userRecord.value());
        UserCreatedEvent deserializedUserEvent = objectMapper.readValue(userJson, UserCreatedEvent.class);

        assertEquals(userEvent.getUserId(), deserializedUserEvent.getUserId());
        assertEquals(userEvent.getUsername(), deserializedUserEvent.getUsername());
        assertEquals(userEvent.getRole(), deserializedUserEvent.getRole());
        // Note: LocalDateTime precision might differ, so we check they're close
        assertTrue(Math.abs(Duration.between(userEvent.getCreatedAt(), 
                deserializedUserEvent.getCreatedAt()).toMillis()) < 1000);
    }

    @Test
    void testAsyncEventPublishing() throws Exception {
        // Arrange
        UserCreatedEvent event = new UserCreatedEvent(
                8L, "asyncuser", "async@example.com", "Async", "User",
                Role.USER, "admin", LocalDateTime.now()
        );

        // Act - Use async publishing
        eventPublisherService.publishEventAsync("test-user-events", "8", event);

        // Give some time for async processing
        Thread.sleep(1000);

        // Assert
        ConsumerRecords<String, Object> records = userEventsConsumer.poll(Duration.ofSeconds(10));
        assertFalse(records.isEmpty());

        ConsumerRecord<String, Object> record = records.iterator().next();
        assertEquals("8", record.key());

        String jsonValue = objectMapper.writeValueAsString(record.value());
        UserCreatedEvent receivedEvent = objectMapper.readValue(jsonValue, UserCreatedEvent.class);
        assertEquals("asyncuser", receivedEvent.getUsername());
    }

    @Test
    void testEventPublishingPerformance() throws Exception {
        // Test publishing multiple events quickly
        int eventCount = 10;
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < eventCount; i++) {
            UserCreatedEvent event = new UserCreatedEvent(
                    (long) (100 + i), "perfuser" + i, "perf" + i + "@example.com", 
                    "Perf", "User" + i, Role.USER, "admin", LocalDateTime.now()
            );
            eventPublisherService.publishUserCreatedEvent(event);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Should be able to publish 10 events in reasonable time (< 5 seconds)
        assertTrue(duration < 5000, "Publishing " + eventCount + " events took too long: " + duration + "ms");

        // Verify all events were received
        ConsumerRecords<String, Object> records = userEventsConsumer.poll(Duration.ofSeconds(15));
        assertEquals(eventCount, records.count());
    }
}