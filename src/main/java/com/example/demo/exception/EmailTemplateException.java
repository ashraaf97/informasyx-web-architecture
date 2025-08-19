package com.example.demo.exception;

/**
 * Exception thrown when there are issues with email template processing.
 * Examples: Template not found, template parsing errors, missing template variables.
 */
public class EmailTemplateException extends EmailException {

    private final String templateName;

    public EmailTemplateException(String message, String templateName) {
        super(message);
        this.templateName = templateName;
    }

    public EmailTemplateException(String message, String templateName, Throwable cause) {
        super(message, cause);
        this.templateName = templateName;
    }

    public EmailTemplateException(String message, String emailAddress, EmailOperation operation, 
                                 String templateName) {
        super(message, emailAddress, operation);
        this.templateName = templateName;
    }

    public EmailTemplateException(String message, String emailAddress, EmailOperation operation, 
                                 String templateName, Throwable cause) {
        super(message, emailAddress, operation, cause);
        this.templateName = templateName;
    }

    public String getTemplateName() {
        return templateName;
    }
}