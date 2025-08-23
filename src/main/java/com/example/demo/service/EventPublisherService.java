package com.example.demo.service;

import com.example.demo.event.AdminCreatedEvent;
import com.example.demo.event.BaseEvent;
import com.example.demo.event.UserCreatedEvent;
import com.example.demo.event.UserRoleChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisherService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.user-events:user-events}")
    private String userEventsTopicName;

    @Value("${app.kafka.topics.admin-events:admin-events}")
    private String adminEventsTopicName;

    public void publishUserCreatedEvent(UserCreatedEvent event) {
        try {
            log.info("Publishing user created event for user: {}", event.getUsername());
            publishEvent(userEventsTopicName, event.getUserId().toString(), event);
        } catch (Exception e) {
            log.error("Failed to publish user created event for user: {}", event.getUsername(), e);
            throw new RuntimeException("Failed to publish user created event", e);
        }
    }

    public void publishAdminCreatedEvent(AdminCreatedEvent event) {
        try {
            log.info("Publishing admin created event for admin: {} created by: {}", 
                    event.getUsername(), event.getCreatedBy());
            publishEvent(adminEventsTopicName, event.getAdminId().toString(), event);
        } catch (Exception e) {
            log.error("Failed to publish admin created event for admin: {}", event.getUsername(), e);
            throw new RuntimeException("Failed to publish admin created event", e);
        }
    }

    public void publishUserRoleChangedEvent(UserRoleChangedEvent event) {
        try {
            log.info("Publishing user role changed event for user: {} from {} to {} by {}", 
                    event.getUsername(), event.getPreviousRole(), event.getNewRole(), event.getChangedBy());
            publishEvent(userEventsTopicName, event.getUserId().toString(), event);
        } catch (Exception e) {
            log.error("Failed to publish user role changed event for user: {}", event.getUsername(), e);
            throw new RuntimeException("Failed to publish user role changed event", e);
        }
    }

    private void publishEvent(String topic, String key, BaseEvent event) {
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, event);
        
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish event {} to topic {}: {}", 
                        event.getEventType(), topic, ex.getMessage(), ex);
            } else {
                log.debug("Successfully published event {} to topic {} with offset: {}", 
                        event.getEventType(), topic, result.getRecordMetadata().offset());
            }
        });
    }

    public void publishEventAsync(String topic, String key, BaseEvent event) {
        // Validate parameters
        if (topic == null) {
            throw new RuntimeException("Topic cannot be null");
        }
        if (key == null) {
            throw new RuntimeException("Key cannot be null");
        }
        if (event == null) {
            throw new RuntimeException("Event cannot be null");
        }
        
        try {
            kafkaTemplate.send(topic, key, event)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to publish async event {} to topic {}: {}", 
                                    event.getEventType(), topic, ex.getMessage());
                        } else {
                            log.debug("Successfully published async event {} to topic {} with offset: {}", 
                                    event.getEventType(), topic, result.getRecordMetadata().offset());
                        }
                    });
        } catch (Exception e) {
            log.error("Exception occurred while publishing async event: ", e);
            // For async operations, we don't want to throw exceptions to the caller
            // The error is already logged, so just return gracefully
        }
    }
}