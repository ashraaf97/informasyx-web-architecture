package com.example.demo.exception;

import com.example.demo.domain.dto.AuthResponse;
import com.example.demo.exception.EmailException.EmailOperation;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        // No additional setup needed
    }

    @Test
    void handleEntityNotFoundException_ShouldReturnNotFound() {
        // Arrange
        EntityNotFoundException exception = new EntityNotFoundException("User not found with id: 123");

        // Act
        ResponseEntity<String> response = globalExceptionHandler.handleEntityNotFoundException(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("User not found with id: 123");
    }

    @Test
    void handleIllegalArgumentException_ShouldReturnBadRequest() {
        // Arrange
        IllegalArgumentException exception = new IllegalArgumentException("Invalid input provided");

        // Act
        ResponseEntity<String> response = globalExceptionHandler.handleIllegalArgumentException(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Invalid input provided");
    }

    @Test
    void handleValidationExceptions_ShouldReturnValidationErrors() throws NoSuchMethodException {
        // Arrange
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "testObject");
        bindingResult.addError(new FieldError("testObject", "username", "Username is required"));
        bindingResult.addError(new FieldError("testObject", "email", "Email should be valid"));

        MethodParameter methodParameter = mock(MethodParameter.class);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(methodParameter, bindingResult);

        // Act
        ResponseEntity<Map<String, String>> response = globalExceptionHandler.handleValidationExceptions(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsEntry("username", "Username is required");
        assertThat(response.getBody()).containsEntry("email", "Email should be valid");
        assertThat(response.getBody()).hasSize(2);
    }

    @Test
    void handleEmailServiceException_EmailVerification_ShouldReturnInternalServerError() {
        // Arrange
        EmailServiceException exception = new EmailServiceException(
                new EmailDeliveryException("SMTP failed", "test@example.com", 
                        EmailOperation.EMAIL_VERIFICATION, 
                        EmailDeliveryException.DeliveryFailureReason.SMTP_CONNECTION_FAILED, 
                        new RuntimeException("Connection refused"))
        );

        // Act
        ResponseEntity<AuthResponse> response = globalExceptionHandler.handleEmailServiceException(exception, webRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).contains("Failed to send email verification");
    }

    @Test
    void handleEmailServiceException_PasswordReset_ShouldReturnInternalServerError() {
        // Arrange
        EmailServiceException exception = new EmailServiceException(
                new EmailDeliveryException("SMTP failed", "test@example.com", 
                        EmailOperation.PASSWORD_RESET, 
                        EmailDeliveryException.DeliveryFailureReason.SMTP_CONNECTION_FAILED, 
                        new RuntimeException("Connection refused"))
        );

        // Act
        ResponseEntity<AuthResponse> response = globalExceptionHandler.handleEmailServiceException(exception, webRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).contains("Failed to send password reset email");
    }

    @Test
    void handleEmailServiceException_WelcomeEmail_ShouldReturnInternalServerError() {
        // Arrange
        EmailServiceException exception = new EmailServiceException(
                new EmailDeliveryException("SMTP failed", "test@example.com", 
                        EmailOperation.WELCOME_EMAIL, 
                        EmailDeliveryException.DeliveryFailureReason.SMTP_CONNECTION_FAILED, 
                        new RuntimeException("Connection refused"))
        );

        // Act
        ResponseEntity<AuthResponse> response = globalExceptionHandler.handleEmailServiceException(exception, webRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).contains("Account created successfully, but welcome email could not be sent");
    }

    @Test
    void handleEmailServiceException_UnknownOperation_ShouldReturnGenericMessage() {
        // Arrange
        EmailServiceException exception = new EmailServiceException(
                new EmailDeliveryException("SMTP failed", "test@example.com", 
                        null, // No operation specified
                        EmailDeliveryException.DeliveryFailureReason.SMTP_CONNECTION_FAILED, 
                        new RuntimeException("Connection refused"))
        );

        // Act
        ResponseEntity<AuthResponse> response = globalExceptionHandler.handleEmailServiceException(exception, webRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).contains("Email service error occurred");
    }

    @Test
    void handleEmailConfigurationException_ShouldReturnServiceUnavailable() {
        // Arrange
        EmailConfigurationException exception = new EmailConfigurationException(
                "SMTP configuration invalid", "smtp.host", new RuntimeException("Config error")
        );

        // Act
        ResponseEntity<AuthResponse> response = globalExceptionHandler.handleEmailConfigurationException(exception, webRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo("Email service is temporarily unavailable. Please try again later.");
    }

    @Test
    void handleEmailDeliveryException_InvalidEmail_ShouldReturnBadRequest() {
        // Arrange
        EmailDeliveryException exception = new EmailDeliveryException(
                "Invalid email format", "invalid-email", 
                EmailOperation.EMAIL_VERIFICATION, 
                EmailDeliveryException.DeliveryFailureReason.INVALID_EMAIL_ADDRESS, 
                new RuntimeException("Email format error")
        );

        // Act
        ResponseEntity<AuthResponse> response = globalExceptionHandler.handleEmailDeliveryException(exception, webRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid email address. Please check your email and try again.");
    }

    @Test
    void handleEmailDeliveryException_RateLimit_ShouldReturnBadRequest() {
        // Arrange
        EmailDeliveryException exception = new EmailDeliveryException(
                "Rate limit exceeded", "test@example.com", 
                EmailOperation.PASSWORD_RESET, 
                EmailDeliveryException.DeliveryFailureReason.RATE_LIMIT_EXCEEDED, 
                new RuntimeException("Too many requests")
        );

        // Act
        ResponseEntity<AuthResponse> response = globalExceptionHandler.handleEmailDeliveryException(exception, webRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo("Too many email requests. Please wait a few minutes and try again.");
    }

    @Test
    void handleEmailDeliveryException_SMTPConnectionFailed_ShouldReturnBadRequest() {
        // Arrange
        EmailDeliveryException exception = new EmailDeliveryException(
                "SMTP connection failed", "test@example.com", 
                EmailOperation.EMAIL_VERIFICATION, 
                EmailDeliveryException.DeliveryFailureReason.SMTP_CONNECTION_FAILED, 
                new RuntimeException("Connection error")
        );

        // Act
        ResponseEntity<AuthResponse> response = globalExceptionHandler.handleEmailDeliveryException(exception, webRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo("Email service is temporarily unavailable. Please try again later.");
    }

    @Test
    void handleEmailDeliveryException_AuthenticationFailed_ShouldReturnBadRequest() {
        // Arrange
        EmailDeliveryException exception = new EmailDeliveryException(
                "Authentication failed", "test@example.com", 
                EmailOperation.WELCOME_EMAIL, 
                EmailDeliveryException.DeliveryFailureReason.AUTHENTICATION_FAILED, 
                new RuntimeException("Auth error")
        );

        // Act
        ResponseEntity<AuthResponse> response = globalExceptionHandler.handleEmailDeliveryException(exception, webRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo("Email service is temporarily unavailable. Please try again later.");
    }

    @Test
    void handleEmailDeliveryException_TemplateProcessingFailed_ShouldReturnBadRequest() {
        // Arrange
        EmailDeliveryException exception = new EmailDeliveryException(
                "Template processing failed", "test@example.com", 
                EmailOperation.EMAIL_VERIFICATION, 
                EmailDeliveryException.DeliveryFailureReason.TEMPLATE_PROCESSING_FAILED, 
                new RuntimeException("Template error")
        );

        // Act
        ResponseEntity<AuthResponse> response = globalExceptionHandler.handleEmailDeliveryException(exception, webRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo("Email processing error. Please try again later.");
    }

    @Test
    void handleEmailTemplateException_ShouldReturnInternalServerError() {
        // Arrange
        EmailTemplateException exception = new EmailTemplateException(
                "Template not found", "test@example.com", 
                EmailOperation.EMAIL_VERIFICATION, "email-verification", 
                new RuntimeException("Template error")
        );

        // Act
        ResponseEntity<AuthResponse> response = globalExceptionHandler.handleEmailTemplateException(exception, webRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo("Email service is temporarily unavailable. Please try again later.");
    }

    @Test
    void handleGenericException_ShouldReturnInternalServerError() {
        // Arrange
        RuntimeException exception = new RuntimeException("Unexpected error occurred");

        // Act
        ResponseEntity<String> response = globalExceptionHandler.handleGenericException(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo("An unexpected error occurred: Unexpected error occurred");
    }

    @Test
    void handleValidationExceptions_SingleError_ShouldReturnSingleError() throws NoSuchMethodException {
        // Arrange
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "testObject");
        bindingResult.addError(new FieldError("testObject", "password", "Password is too weak"));

        MethodParameter methodParameter = mock(MethodParameter.class);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(methodParameter, bindingResult);

        // Act
        ResponseEntity<Map<String, String>> response = globalExceptionHandler.handleValidationExceptions(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsEntry("password", "Password is too weak");
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void handleValidationExceptions_EmptyErrors_ShouldReturnEmptyMap() throws NoSuchMethodException {
        // Arrange
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "testObject");
        // No errors added

        MethodParameter methodParameter = mock(MethodParameter.class);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(methodParameter, bindingResult);

        // Act
        ResponseEntity<Map<String, String>> response = globalExceptionHandler.handleValidationExceptions(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void handleEntityNotFoundException_NullMessage_ShouldHandleGracefully() {
        // Arrange
        EntityNotFoundException exception = new EntityNotFoundException((String) null);

        // Act
        ResponseEntity<String> response = globalExceptionHandler.handleEntityNotFoundException(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void handleGenericException_NullMessage_ShouldHandleGracefully() {
        // Arrange
        RuntimeException exception = new RuntimeException((String) null);

        // Act
        ResponseEntity<String> response = globalExceptionHandler.handleGenericException(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo("An unexpected error occurred: null");
    }

    @Test
    void handleEmailDeliveryException_UnknownReason_ShouldReturnDefaultMessage() {
        // Arrange - Create exception with all possible enum values to ensure coverage
        for (EmailDeliveryException.DeliveryFailureReason reason : EmailDeliveryException.DeliveryFailureReason.values()) {
            EmailDeliveryException exception = new EmailDeliveryException(
                    "Test failure", "test@example.com", 
                    EmailOperation.GENERAL, reason, 
                    new RuntimeException("Test error")
            );

            // Act
            ResponseEntity<AuthResponse> response = globalExceptionHandler.handleEmailDeliveryException(exception, webRequest);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().isSuccess()).isFalse();
            assertThat(response.getBody().getMessage()).isNotEmpty();
        }
    }
}