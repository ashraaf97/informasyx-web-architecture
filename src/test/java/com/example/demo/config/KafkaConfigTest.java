package com.example.demo.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class KafkaConfigTest {

    private KafkaConfig kafkaConfig;

    @BeforeEach
    void setUp() {
        kafkaConfig = new KafkaConfig();
        ReflectionTestUtils.setField(kafkaConfig, "bootstrapServers", "localhost:9092");
    }

    @Test
    void testProducerFactory() {
        // Act
        ProducerFactory<String, Object> producerFactory = kafkaConfig.producerFactory();

        // Assert
        assertNotNull(producerFactory);
        assertTrue(producerFactory instanceof DefaultKafkaProducerFactory);

        // Get configuration properties
        Map<String, Object> configProps = producerFactory.getConfigurationProperties();
        
        assertEquals("localhost:9092", configProps.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
        assertEquals(StringSerializer.class, configProps.get(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG));
        assertEquals(JsonSerializer.class, configProps.get(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG));
        assertEquals("all", configProps.get(ProducerConfig.ACKS_CONFIG));
        assertEquals(3, configProps.get(ProducerConfig.RETRIES_CONFIG));
        assertEquals(1000, configProps.get(ProducerConfig.RETRY_BACKOFF_MS_CONFIG));
        assertEquals(30000, configProps.get(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG));
        assertEquals(true, configProps.get(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG));
    }

    @Test
    void testKafkaTemplate() {
        // Act
        KafkaTemplate<String, Object> kafkaTemplate = kafkaConfig.kafkaTemplate();

        // Assert
        assertNotNull(kafkaTemplate);
        assertNotNull(kafkaTemplate.getProducerFactory());
        assertTrue(kafkaTemplate.getProducerFactory() instanceof DefaultKafkaProducerFactory);
    }

    @Test
    void testProducerFactoryWithCustomBootstrapServers() {
        // Arrange
        ReflectionTestUtils.setField(kafkaConfig, "bootstrapServers", "custom-server:9093");

        // Act
        ProducerFactory<String, Object> producerFactory = kafkaConfig.producerFactory();

        // Assert
        Map<String, Object> configProps = producerFactory.getConfigurationProperties();
        assertEquals("custom-server:9093", configProps.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
    }

    @Test
    void testProducerFactoryReliabilitySettings() {
        // Act
        ProducerFactory<String, Object> producerFactory = kafkaConfig.producerFactory();
        Map<String, Object> configProps = producerFactory.getConfigurationProperties();

        // Assert - Verify reliability settings
        assertEquals("all", configProps.get(ProducerConfig.ACKS_CONFIG));
        assertEquals(3, configProps.get(ProducerConfig.RETRIES_CONFIG));
        assertEquals(true, configProps.get(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG));
        assertEquals(1000, configProps.get(ProducerConfig.RETRY_BACKOFF_MS_CONFIG));
        assertEquals(30000, configProps.get(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG));
    }

    @Test
    void testProducerFactorySerializers() {
        // Act
        ProducerFactory<String, Object> producerFactory = kafkaConfig.producerFactory();
        Map<String, Object> configProps = producerFactory.getConfigurationProperties();

        // Assert - Verify serializers
        assertEquals(StringSerializer.class, configProps.get(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG));
        assertEquals(JsonSerializer.class, configProps.get(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG));
    }

    @Test
    void testKafkaTemplateConfiguration() {
        // Act
        KafkaTemplate<String, Object> kafkaTemplate = kafkaConfig.kafkaTemplate();
        ProducerFactory<String, Object> producerFactory = kafkaTemplate.getProducerFactory();

        // Assert
        assertNotNull(producerFactory);
        Map<String, Object> configProps = producerFactory.getConfigurationProperties();
        assertEquals("localhost:9092", configProps.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
    }

    @Test
    void testBeanCreation() {
        // Act & Assert - Verify beans can be created without exceptions
        assertDoesNotThrow(() -> {
            ProducerFactory<String, Object> producerFactory = kafkaConfig.producerFactory();
            assertNotNull(producerFactory);
        });

        assertDoesNotThrow(() -> {
            KafkaTemplate<String, Object> kafkaTemplate = kafkaConfig.kafkaTemplate();
            assertNotNull(kafkaTemplate);
        });
    }

    @Test
    void testProducerFactoryIsDefaultKafkaProducerFactory() {
        // Act
        ProducerFactory<String, Object> producerFactory = kafkaConfig.producerFactory();

        // Assert
        assertInstanceOf(DefaultKafkaProducerFactory.class, producerFactory);
        DefaultKafkaProducerFactory<String, Object> defaultFactory = 
            (DefaultKafkaProducerFactory<String, Object>) producerFactory;
        
        assertNotNull(defaultFactory.getConfigurationProperties());
        assertFalse(defaultFactory.getConfigurationProperties().isEmpty());
    }

    @Test
    void testConfigurationPropertiesNotNull() {
        // Act
        ProducerFactory<String, Object> producerFactory = kafkaConfig.producerFactory();
        Map<String, Object> configProps = producerFactory.getConfigurationProperties();

        // Assert
        assertNotNull(configProps);
        assertFalse(configProps.isEmpty());
        
        // Verify all required properties are present
        assertTrue(configProps.containsKey(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
        assertTrue(configProps.containsKey(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG));
        assertTrue(configProps.containsKey(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG));
        assertTrue(configProps.containsKey(ProducerConfig.ACKS_CONFIG));
        assertTrue(configProps.containsKey(ProducerConfig.RETRIES_CONFIG));
        assertTrue(configProps.containsKey(ProducerConfig.RETRY_BACKOFF_MS_CONFIG));
        assertTrue(configProps.containsKey(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG));
        assertTrue(configProps.containsKey(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG));
    }
}