package com.example.demo.adapter.whatsappadapter;

import com.example.demo.adapter.whatsappadapter.WhatsappAdapter.WhatsAppRequest;
import com.example.demo.adapter.whatsappadapter.WhatsappAdapter.WhatsAppResponse;
import com.example.demo.adapter.whatsappadapter.WhatsappAdapter.WhatsAppException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WhatsappAdapterTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private WhatsappAdapter whatsappAdapter;

    private final String testApiKey = "test-api-key";
    private final String testEndpoint = "https://ustazai.my/send-message";
    private final String testSender = "0162888xxxx";
    private final String testRecipient = "0172888xxxx";
    private final String testMessage = "Hello World";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(whatsappAdapter, "apiKey", testApiKey);
        ReflectionTestUtils.setField(whatsappAdapter, "apiEndpoint", testEndpoint);
    }

    @Test
    void testSendMessagePost_Success() {
        // Arrange
        WhatsAppResponse expectedResponse = WhatsAppResponse.builder()
                .success(true)
                .message("Message sent successfully")
                .build();
        
        ResponseEntity<WhatsAppResponse> responseEntity = ResponseEntity.ok(expectedResponse);
        
        when(restTemplate.exchange(
            eq(testEndpoint),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(WhatsAppResponse.class)
        )).thenReturn(responseEntity);

        // Act
        WhatsAppResponse result = whatsappAdapter.sendMessagePost(testSender, testRecipient, testMessage);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("Message sent successfully", result.getMessage());
        
        verify(restTemplate).exchange(
            eq(testEndpoint),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(WhatsAppResponse.class)
        );
    }

    @Test
    void testSendMessageGet_Success() {
        // Arrange
        WhatsAppResponse expectedResponse = WhatsAppResponse.builder()
                .success(true)
                .message("Message sent successfully")
                .build();
        
        ResponseEntity<WhatsAppResponse> responseEntity = ResponseEntity.ok(expectedResponse);
        
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            isNull(),
            eq(WhatsAppResponse.class)
        )).thenReturn(responseEntity);

        // Act
        WhatsAppResponse result = whatsappAdapter.sendMessageGet(testSender, testRecipient, testMessage);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("Message sent successfully", result.getMessage());
        
        verify(restTemplate).exchange(
            anyString(),
            eq(HttpMethod.GET),
            isNull(),
            eq(WhatsAppResponse.class)
        );
    }

    @Test
    void testSendMessage_DefaultsToPost() {
        // Arrange
        WhatsAppResponse expectedResponse = WhatsAppResponse.builder()
                .success(true)
                .message("Message sent successfully")
                .build();
        
        ResponseEntity<WhatsAppResponse> responseEntity = ResponseEntity.ok(expectedResponse);
        
        when(restTemplate.exchange(
            eq(testEndpoint),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(WhatsAppResponse.class)
        )).thenReturn(responseEntity);

        // Act
        WhatsAppResponse result = whatsappAdapter.sendMessage(testSender, testRecipient, testMessage);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("Message sent successfully", result.getMessage());
    }

    @Test
    void testSendMessagePost_RestClientException() {
        // Arrange
        when(restTemplate.exchange(
            eq(testEndpoint),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(WhatsAppResponse.class)
        )).thenThrow(new RestClientException("API Error"));

        // Act & Assert
        WhatsAppException exception = assertThrows(WhatsAppException.class, () -> {
            whatsappAdapter.sendMessagePost(testSender, testRecipient, testMessage);
        });
        
        assertTrue(exception.getMessage().contains("Failed to send WhatsApp message via POST"));
        assertTrue(exception.getCause() instanceof RestClientException);
    }

    @Test
    void testValidateInputs_NullSender() {
        // Act & Assert
        jakarta.validation.ConstraintViolationException exception = assertThrows(jakarta.validation.ConstraintViolationException.class, () -> {
            whatsappAdapter.sendMessage(null, testRecipient, testMessage);
        });
        
        assertTrue(exception.getMessage().contains("Sender number is required"));
    }

    @Test
    void testValidateInputs_EmptySender() {
        // Act & Assert
        jakarta.validation.ConstraintViolationException exception = assertThrows(jakarta.validation.ConstraintViolationException.class, () -> {
            whatsappAdapter.sendMessage("", testRecipient, testMessage);
        });
        
        assertTrue(exception.getMessage().contains("Sender number is required"));
    }

    @Test
    void testValidateInputs_NullRecipient() {
        // Act & Assert
        jakarta.validation.ConstraintViolationException exception = assertThrows(jakarta.validation.ConstraintViolationException.class, () -> {
            whatsappAdapter.sendMessage(testSender, null, testMessage);
        });
        
        assertTrue(exception.getMessage().contains("Recipient number is required"));
    }

    @Test
    void testValidateInputs_EmptyRecipient() {
        // Act & Assert
        jakarta.validation.ConstraintViolationException exception = assertThrows(jakarta.validation.ConstraintViolationException.class, () -> {
            whatsappAdapter.sendMessage(testSender, "", testMessage);
        });
        
        assertTrue(exception.getMessage().contains("Recipient number is required"));
    }

    @Test
    void testValidateInputs_NullMessage() {
        // Act & Assert
        jakarta.validation.ConstraintViolationException exception = assertThrows(jakarta.validation.ConstraintViolationException.class, () -> {
            whatsappAdapter.sendMessage(testSender, testRecipient, null);
        });
        
        assertTrue(exception.getMessage().contains("Message is required"));
    }

    @Test
    void testValidateInputs_EmptyMessage() {
        // Act & Assert
        jakarta.validation.ConstraintViolationException exception = assertThrows(jakarta.validation.ConstraintViolationException.class, () -> {
            whatsappAdapter.sendMessage(testSender, testRecipient, "");
        });
        
        assertTrue(exception.getMessage().contains("Message is required"));
    }

    @Test
    void testValidateInputs_EmptyApiKey() {
        // Arrange
        ReflectionTestUtils.setField(whatsappAdapter, "apiKey", "");

        // Act & Assert
        jakarta.validation.ConstraintViolationException exception = assertThrows(jakarta.validation.ConstraintViolationException.class, () -> {
            whatsappAdapter.sendMessage(testSender, testRecipient, testMessage);
        });
        
        assertTrue(exception.getMessage().contains("API key is required"));
    }

    @Test
    void testWhatsAppRequest_Builder() {
        // Act
        WhatsAppRequest request = WhatsAppRequest.builder()
                .apiKey(testApiKey)
                .sender(testSender)
                .number(testRecipient)
                .message(testMessage)
                .build();

        // Assert
        assertEquals(testApiKey, request.getApiKey());
        assertEquals(testSender, request.getSender());
        assertEquals(testRecipient, request.getNumber());
        assertEquals(testMessage, request.getMessage());
    }

    @Test
    void testWhatsAppResponse_Builder() {
        // Arrange
        String testResponseMessage = "Test message";
        String testError = "Test error";

        // Act
        WhatsAppResponse response = WhatsAppResponse.builder()
                .success(true)
                .message(testResponseMessage)
                .error(testError)
                .build();

        // Assert
        assertTrue(response.isSuccess());
        assertEquals(testResponseMessage, response.getMessage());
        assertEquals(testError, response.getError());
        assertNotNull(response.getData());
    }
}
