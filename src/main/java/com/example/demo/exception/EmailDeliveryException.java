package com.example.demo.exception;

import lombok.Getter;

/**
 * Exception thrown when email delivery fails.
 * Examples: SMTP connection failures, SES sending errors, invalid email addresses.
 */
public class EmailDeliveryException extends EmailException {

    private final DeliveryFailureReason reason;

    public EmailDeliveryException(String message, String emailAddress, EmailOperation operation) {
        super(message, emailAddress, operation);
        this.reason = DeliveryFailureReason.UNKNOWN;
    }

    public EmailDeliveryException(String message, String emailAddress, EmailOperation operation, Throwable cause) {
        super(message, emailAddress, operation, cause);
        this.reason = DeliveryFailureReason.UNKNOWN;
    }

    public EmailDeliveryException(String message, String emailAddress, EmailOperation operation, 
                                 DeliveryFailureReason reason) {
        super(message, emailAddress, operation);
        this.reason = reason;
    }

    public EmailDeliveryException(String message, String emailAddress, EmailOperation operation, 
                                 DeliveryFailureReason reason, Throwable cause) {
        super(message, emailAddress, operation, cause);
        this.reason = reason;
    }

    public DeliveryFailureReason getReason() {
        return reason;
    }

    /**
     * Enum to specify the specific reason for delivery failure
     */
    @Getter
    public enum DeliveryFailureReason {
        INVALID_EMAIL_ADDRESS("Invalid email address"),
        SMTP_CONNECTION_FAILED("SMTP connection failed"),
        AUTHENTICATION_FAILED("Authentication failed"),
        RATE_LIMIT_EXCEEDED("Rate limit exceeded"),
        SERVICE_UNAVAILABLE("Email service unavailable"),
        TEMPLATE_PROCESSING_FAILED("Email template processing failed"),
        AWS_SES_ERROR("AWS SES service error"),
        UNKNOWN("Unknown delivery failure");

        private final String description;

        DeliveryFailureReason(String description) {
            this.description = description;
        }

    }
}