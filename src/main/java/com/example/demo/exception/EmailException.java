package com.example.demo.exception;

/**
 * Base exception for all email-related operations.
 * This is the parent class for all email exceptions in the system.
 */
public class EmailException extends Exception {

    private final String emailAddress;
    private final EmailOperation operation;

    public EmailException(String message) {
        super(message);
        this.emailAddress = null;
        this.operation = null;
    }

    public EmailException(String message, Throwable cause) {
        super(message, cause);
        this.emailAddress = null;
        this.operation = null;
    }

    public EmailException(String message, String emailAddress, EmailOperation operation) {
        super(message);
        this.emailAddress = emailAddress;
        this.operation = operation;
    }

    public EmailException(String message, String emailAddress, EmailOperation operation, Throwable cause) {
        super(message, cause);
        this.emailAddress = emailAddress;
        this.operation = operation;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public EmailOperation getOperation() {
        return operation;
    }

    /**
     * Enum to specify the type of email operation that failed
     */
    public enum EmailOperation {
        EMAIL_VERIFICATION("Email Verification"),
        PASSWORD_RESET("Password Reset"),
        WELCOME_EMAIL("Welcome Email"),
        GENERAL("General Email");

        private final String displayName;

        EmailOperation(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}