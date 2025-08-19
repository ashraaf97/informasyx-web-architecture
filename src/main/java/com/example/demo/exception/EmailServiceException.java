package com.example.demo.exception;

/**
 * Exception thrown when there are general email service errors.
 * This is a runtime version of EmailException for cases where checked exceptions
 * cannot be used (like in Spring's declarative transaction management).
 */
public class EmailServiceException extends RuntimeException {

    private final String emailAddress;
    private final EmailException.EmailOperation operation;

    public EmailServiceException(String message) {
        super(message);
        this.emailAddress = null;
        this.operation = null;
    }

    public EmailServiceException(String message, Throwable cause) {
        super(message, cause);
        this.emailAddress = null;
        this.operation = null;
    }

    public EmailServiceException(String message, String emailAddress, EmailException.EmailOperation operation) {
        super(message);
        this.emailAddress = emailAddress;
        this.operation = operation;
    }

    public EmailServiceException(String message, String emailAddress, EmailException.EmailOperation operation, Throwable cause) {
        super(message, cause);
        this.emailAddress = emailAddress;
        this.operation = operation;
    }

    /**
     * Create EmailServiceException from EmailException
     */
    public EmailServiceException(EmailException emailException) {
        super(emailException.getMessage(), emailException);
        this.emailAddress = emailException.getEmailAddress();
        this.operation = emailException.getOperation();
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public EmailException.EmailOperation getOperation() {
        return operation;
    }
}