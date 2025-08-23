package com.example.demo.exception;

import lombok.Getter;

/**
 * Exception thrown when there are configuration issues with email services.
 * Examples: Missing credentials, invalid SMTP settings, AWS SES configuration errors.
 */
@Getter
public class EmailConfigurationException extends EmailException {

    private final String configurationKey;

    public EmailConfigurationException(String message) {
        super(message);
        this.configurationKey = null;
    }

    public EmailConfigurationException(String message, Throwable cause) {
        super(message, cause);
        this.configurationKey = null;
    }

    public EmailConfigurationException(String message, String configurationKey) {
        super(message);
        this.configurationKey = configurationKey;
    }

    public EmailConfigurationException(String message, String configurationKey, Throwable cause) {
        super(message, cause);
        this.configurationKey = configurationKey;
    }

}