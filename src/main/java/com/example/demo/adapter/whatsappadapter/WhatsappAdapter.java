package com.example.demo.adapter.whatsappadapter;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@Validated
public class WhatsappAdapter {

    private final RestTemplate restTemplate;

    @Value("${whatsapp.api.endpoint:https://ustazai.my/send-message}")
    private String apiEndpoint;

    @Value("${whatsapp.api.key:}")
    private String apiKey;

    /**
     * Send WhatsApp message using POST method
     * 
     * @param sender The sender's phone number
     * @param recipientNumber The recipient's phone number
     * @param message The message to send
     * @return WhatsAppResponse containing the API response
     * @throws WhatsAppException if the API call fails
     */
    public WhatsAppResponse sendMessagePost(@NotBlank String sender, @NotBlank String recipientNumber, @NotBlank String message) {
        log.info("Sending WhatsApp message via POST to number: {}", recipientNumber);
        
        WhatsAppRequest request = WhatsAppRequest.builder()
                .apiKey(apiKey)
                .sender(sender)
                .number(recipientNumber)
                .message(message)
                .build();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<WhatsAppRequest> entity = new HttpEntity<>(request, headers);
        
        try {
            ResponseEntity<WhatsAppResponse> response = restTemplate.exchange(
                apiEndpoint,
                HttpMethod.POST,
                entity,
                WhatsAppResponse.class
            );
            
            log.info("WhatsApp message sent successfully via POST. Response: {}", response.getBody());
            return response.getBody();
            
        } catch (RestClientException e) {
            log.error("Failed to send WhatsApp message via POST: {}", e.getMessage());
            throw new WhatsAppException("Failed to send WhatsApp message via POST", e);
        }
    }

    /**
     * Send WhatsApp message using GET method
     * 
     * @param sender The sender's phone number
     * @param recipientNumber The recipient's phone number
     * @param message The message to send
     * @return WhatsAppResponse containing the API response
     * @throws WhatsAppException if the API call fails
     */
    public WhatsAppResponse sendMessageGet(@NotBlank String sender, @NotBlank String recipientNumber, @NotBlank String message) {
        log.info("Sending WhatsApp message via GET to number: {}", recipientNumber);
        
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(apiEndpoint)
                .queryParam("api_key", apiKey)
                .queryParam("sender", sender)
                .queryParam("number", recipientNumber)
                .queryParam("message", message);
        
        try {
            ResponseEntity<WhatsAppResponse> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                null,
                WhatsAppResponse.class
            );
            
            log.info("WhatsApp message sent successfully via GET. Response: {}", response.getBody());
            return response.getBody();
            
        } catch (RestClientException e) {
            log.error("Failed to send WhatsApp message via GET: {}", e.getMessage());
            throw new WhatsAppException("Failed to send WhatsApp message via GET", e);
        }
    }

    /**
     * Send WhatsApp message (defaults to POST method)
     * 
     * @param sender The sender's phone number
     * @param recipientNumber The recipient's phone number
     * @param message The message to send
     * @return WhatsAppResponse containing the API response
     * @throws WhatsAppException if the API call fails
     */
    public WhatsAppResponse sendMessage(@NotBlank String sender, @NotBlank String recipientNumber, @NotBlank String message) {
        return sendMessagePost(sender, recipientNumber, message);
    }


    /**
     * Request DTO for WhatsApp API
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Valid
    public static class WhatsAppRequest {
        @NotBlank(message = "API key is required")
        @JsonProperty("api_key")
        private String apiKey;
        
        @NotBlank(message = "Sender number is required")
        private String sender;
        
        @NotBlank(message = "Recipient number is required")
        private String number;
        
        @NotBlank(message = "Message is required")
        private String message;
    }

    /**
     * Response DTO for WhatsApp API
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WhatsAppResponse {
        private boolean success;
        private String message;
        private String error;
        @Builder.Default
        private Map<String, Object> data = new HashMap<>();
    }

    /**
     * Custom exception for WhatsApp API errors
     */
    public static class WhatsAppException extends RuntimeException {
        public WhatsAppException(String message) {
            super(message);
        }

        public WhatsAppException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
