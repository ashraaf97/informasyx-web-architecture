package com.example.demo.exception;

import com.example.demo.domain.dto.AuthResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFoundException(EntityNotFoundException ex) {
        log.error("Entity not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Illegal argument: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.error("Validation errors: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.error("Invalid JSON request: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid JSON format in request body");
    }

    /**
     * Handle EmailServiceException (runtime email exceptions)
     */
    @ExceptionHandler(EmailServiceException.class)
    public ResponseEntity<AuthResponse> handleEmailServiceException(EmailServiceException ex, WebRequest request) {
        log.error("Email service error for operation {}: {}", 
                 ex.getOperation() != null ? ex.getOperation().getDisplayName() : "Unknown", 
                 ex.getMessage(), ex);

        String userMessage = getUserFriendlyMessage(ex);
        AuthResponse response = AuthResponse.failure(userMessage);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * Handle EmailConfigurationException specifically
     */
    @ExceptionHandler(EmailConfigurationException.class)
    public ResponseEntity<AuthResponse> handleEmailConfigurationException(EmailConfigurationException ex, WebRequest request) {
        log.error("Email configuration error for key {}: {}", 
                 ex.getConfigurationKey(), ex.getMessage(), ex);

        AuthResponse response = AuthResponse.failure("Email service is temporarily unavailable. Please try again later.");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    /**
     * Handle EmailDeliveryException specifically
     */
    @ExceptionHandler(EmailDeliveryException.class)
    public ResponseEntity<AuthResponse> handleEmailDeliveryException(EmailDeliveryException ex, WebRequest request) {
        log.error("Email delivery error for {} to {}: {} - {}", 
                 ex.getOperation() != null ? ex.getOperation().getDisplayName() : "Unknown",
                 ex.getEmailAddress(),
                 ex.getReason().getDescription(),
                 ex.getMessage(), ex);

        String userMessage = getDeliveryErrorMessage(ex);
        AuthResponse response = AuthResponse.failure(userMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle EmailTemplateException specifically
     */
    @ExceptionHandler(EmailTemplateException.class)
    public ResponseEntity<AuthResponse> handleEmailTemplateException(EmailTemplateException ex, WebRequest request) {
        log.error("Email template error for template {} and operation {}: {}", 
                 ex.getTemplateName(),
                 ex.getOperation() != null ? ex.getOperation().getDisplayName() : "Unknown", 
                 ex.getMessage(), ex);

        AuthResponse response = AuthResponse.failure("Email service is temporarily unavailable. Please try again later.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * Generate user-friendly error messages for email service exceptions
     */
    private String getUserFriendlyMessage(EmailServiceException ex) {
        if (ex.getOperation() == null) {
            return "Email service error occurred. Please try again later.";
        }

        switch (ex.getOperation()) {
            case EMAIL_VERIFICATION:
                return "Failed to send email verification. Please check your email address and try again.";
            case PASSWORD_RESET:
                return "Failed to send password reset email. Please check your email address and try again.";
            case WELCOME_EMAIL:
                return "Account created successfully, but welcome email could not be sent.";
            case GENERAL:
            default:
                return "Email service error occurred. Please try again later.";
        }
    }

    /**
     * Generate user-friendly error messages for delivery exceptions
     */
    private String getDeliveryErrorMessage(EmailDeliveryException ex) {
        return switch (ex.getReason()) {
            case INVALID_EMAIL_ADDRESS -> "Invalid email address. Please check your email and try again.";
            case RATE_LIMIT_EXCEEDED -> "Too many email requests. Please wait a few minutes and try again.";
            case AUTHENTICATION_FAILED, SMTP_CONNECTION_FAILED, AWS_SES_ERROR, SERVICE_UNAVAILABLE ->
                    "Email service is temporarily unavailable. Please try again later.";
            case TEMPLATE_PROCESSING_FAILED -> "Email processing error. Please try again later.";
            default -> "Failed to send email. Please check your email address and try again.";
        };
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An unexpected error occurred: " + ex.getMessage());
    }
} 