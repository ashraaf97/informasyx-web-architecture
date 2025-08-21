# Custom Email Exception Handling

This document describes the custom exception hierarchy implemented for email operations in the Informasyx application.

## Overview

Instead of using generic `RuntimeException` for email-related errors, the system now uses a comprehensive hierarchy of specific exceptions that provide detailed error information and user-friendly messaging.

## Exception Hierarchy

### Base Exceptions

#### EmailException (Checked Exception)
```java
public class EmailException extends Exception
```
- Base class for all email-related operations
- Contains email address and operation type information
- Includes `EmailOperation` enum for operation classification

#### EmailServiceException (Runtime Exception)
```java
public class EmailServiceException extends RuntimeException
```
- Runtime wrapper for email exceptions
- Used when checked exceptions cannot be thrown
- Can wrap other email exceptions

### Specific Exception Types

#### 1. EmailConfigurationException
```java
public class EmailConfigurationException extends EmailException
```
**Purpose**: Configuration issues with email services
**Examples**: 
- Missing AWS SES credentials
- Invalid SMTP settings
- Incorrect region configuration

**Usage**:
```java
throw new EmailConfigurationException(
    "Failed to initialize AWS SES client", 
    "aws.ses", 
    cause
);
```

#### 2. EmailDeliveryException
```java
public class EmailDeliveryException extends EmailException
```
**Purpose**: Email delivery failures
**Includes**: `DeliveryFailureReason` enum for specific failure types

**Failure Reasons**:
- `INVALID_EMAIL_ADDRESS`
- `SMTP_CONNECTION_FAILED` 
- `AUTHENTICATION_FAILED`
- `RATE_LIMIT_EXCEEDED`
- `AWS_SES_ERROR`
- `SERVICE_UNAVAILABLE`
- `TEMPLATE_PROCESSING_FAILED`
- `UNKNOWN`

**Usage**:
```java
throw new EmailDeliveryException(
    "Failed to send email verification via SMTP", 
    user.getEmail(), 
    EmailOperation.EMAIL_VERIFICATION,
    DeliveryFailureReason.SMTP_CONNECTION_FAILED,
    cause
);
```

#### 3. EmailTemplateException
```java
public class EmailTemplateException extends EmailException
```
**Purpose**: Email template processing errors
**Examples**:
- Template not found
- Template parsing errors
- Missing template variables

**Usage**:
```java
throw new EmailTemplateException(
    "Failed to process email verification template",
    user.getEmail(),
    EmailOperation.EMAIL_VERIFICATION,
    "email-verification",
    cause
);
```

## Email Operations Enum

```java
public enum EmailOperation {
    EMAIL_VERIFICATION("Email Verification"),
    PASSWORD_RESET("Password Reset"),
    WELCOME_EMAIL("Welcome Email"),
    GENERAL("General Email");
}
```

## Global Exception Handler

The `GlobalExceptionHandler` provides centralized exception handling with:

### Features
- **User-friendly messages**: Internal error details are not exposed
- **Proper HTTP status codes**: Different status codes based on error type
- **Comprehensive logging**: Detailed logging with context information
- **Operation-specific handling**: Different messages based on email operation type

### Exception Handling Methods

```java
@ExceptionHandler(EmailServiceException.class)
public ResponseEntity<AuthResponse> handleEmailServiceException(EmailServiceException ex)

@ExceptionHandler(EmailConfigurationException.class) 
public ResponseEntity<AuthResponse> handleEmailConfigurationException(EmailConfigurationException ex)

@ExceptionHandler(EmailDeliveryException.class)
public ResponseEntity<AuthResponse> handleEmailDeliveryException(EmailDeliveryException ex)

@ExceptionHandler(EmailTemplateException.class)
public ResponseEntity<AuthResponse> handleEmailTemplateException(EmailTemplateException ex)
```

### HTTP Status Code Mapping

| Exception Type | HTTP Status | Reason |
|---|---|---|
| `EmailServiceException` | 500 Internal Server Error | General email service failure |
| `EmailConfigurationException` | 503 Service Unavailable | Configuration issue - service unavailable |
| `EmailDeliveryException` | 400 Bad Request | Usually user-correctable (invalid email) |
| `EmailTemplateException` | 500 Internal Server Error | Internal template processing issue |

## User-Friendly Error Messages

The system provides context-appropriate error messages:

### Email Verification Errors
- "Failed to send email verification. Please check your email address and try again."

### Password Reset Errors  
- "Failed to send password reset email. Please check your email address and try again."

### Welcome Email Errors
- "Account created successfully, but welcome email could not be sent."

### Specific Delivery Errors
- "Invalid email address. Please check your email and try again."
- "Too many email requests. Please wait a few minutes and try again."
- "Email service is temporarily unavailable. Please try again later."

## Implementation Examples

### EmailServiceImpl (SMTP)
```java
try {
    // Email sending logic
    mailSender.send(message);
} catch (TemplateEngineException e) {
    throw new EmailServiceException(new EmailTemplateException(
        "Failed to process email verification template", 
        user.getPerson().getEmail(), 
        EmailOperation.EMAIL_VERIFICATION, 
        "email-verification", 
        e));
} catch (MessagingException e) {
    throw new EmailServiceException(new EmailDeliveryException(
        "Failed to send email verification via SMTP", 
        user.getPerson().getEmail(), 
        EmailOperation.EMAIL_VERIFICATION, 
        EmailDeliveryException.DeliveryFailureReason.SMTP_CONNECTION_FAILED, 
        e));
}
```

### SesEmailServiceImpl (AWS SES)
```java
try {
    // SES client initialization  
    this.sesClient = AmazonSimpleEmailServiceClientBuilder.standard()...
} catch (Exception e) {
    throw new EmailServiceException(new EmailConfigurationException(
        "Failed to initialize AWS SES client. Check your AWS credentials and region configuration.", 
        "aws.ses", 
        e));
}
```

## Benefits

1. **Specific Exception Types**: Replace generic `RuntimeException` with meaningful exceptions
2. **Rich Context Information**: Include email address, operation type, and failure reason
3. **User-Friendly Messaging**: Provide helpful error messages without exposing internal details
4. **Proper Status Codes**: Return appropriate HTTP status codes for different error types
5. **Comprehensive Logging**: Log detailed information for debugging while showing simple messages to users
6. **Flexible Error Handling**: Different handling strategies for different email operations
7. **Maintainable Code**: Clear separation of concerns and easy to extend

## Testing

The custom exceptions can be tested by:

1. **Configuration Errors**: Set invalid AWS SES credentials or SMTP settings
2. **Delivery Errors**: Use invalid email addresses or unreachable SMTP servers
3. **Template Errors**: Remove or corrupt email templates
4. **Rate Limiting**: Trigger rate limiting with email providers

All exceptions are properly caught and handled by the `GlobalExceptionHandler`, providing consistent error responses to the client while logging detailed information for developers.