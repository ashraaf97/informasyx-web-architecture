package com.example.demo.config;

import com.example.demo.service.EventPublisherService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@TestConfiguration
@Profile("!embedded-kafka")
public class TestKafkaConfiguration {

    @Bean
    @Primary
    public EventPublisherService eventPublisherService() {
        return Mockito.mock(EventPublisherService.class);
    }
}