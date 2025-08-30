package com.example.demo.service.impl;

import com.example.demo.domain.Person;
import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import com.example.demo.exception.EmailServiceException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.exceptions.TemplateEngineException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailServiceImpl emailService;

    private User testUser;
    private Person testPerson;

    @BeforeEach
    void setUp() {
        // Set up test data
        testPerson = new Person();
        testPerson.setId(1L);
        testPerson.setFirstName("John");
        testPerson.setLastName("Doe");
        testPerson.setEmail("john.doe@example.com");
        testPerson.setPhoneNumber("1234567890");
        testPerson.setAddress("123 Main St");

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("johndoe");
        testUser.setPassword("password123");
        testUser.setPerson(testPerson);
        testUser.setActive(true);
        testUser.setEmailVerified(false);
        testUser.setRole(Role.USER);

        // Set up mock values using ReflectionTestUtils
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@informasyx.com");
        ReflectionTestUtils.setField(emailService, "baseUrl", "http://localhost:8080");
    }

    @Test
    void sendEmailVerification_Success() throws Exception {
        // Arrange
        String token = "verification-token-123";
        String expectedContent = "<html>Email verification content</html>";
        
        when(templateEngine.process(eq("email-verification"), any(Context.class)))
                .thenReturn(expectedContent);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        // Act
        assertDoesNotThrow(() -> {
            emailService.sendEmailVerification(testUser, token);
        });

        // Assert
        verify(templateEngine).process(eq("email-verification"), any(Context.class));
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendEmailVerification_TemplateEngineException_ShouldThrowEmailServiceException() {
        // Arrange
        String token = "verification-token-123";
        RuntimeException templateException = new RuntimeException("Template not found");
        
        when(templateEngine.process(eq("email-verification"), any(Context.class)))
                .thenThrow(templateException);

        // Act & Assert
        EmailServiceException exception = assertThrows(EmailServiceException.class, () -> {
            emailService.sendEmailVerification(testUser, token);
        });

        assertNotNull(exception.getCause());
        verify(templateEngine).process(eq("email-verification"), any(Context.class));
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void sendEmailVerification_MessagingException_ShouldThrowEmailServiceException() throws Exception {
        // Arrange
        String token = "verification-token-123";
        String expectedContent = "<html>Email verification content</html>";
        MessagingException messagingException = new MessagingException("SMTP server unavailable");
        
        when(templateEngine.process(eq("email-verification"), any(Context.class)))
                .thenReturn(expectedContent);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doAnswer(invocation -> { throw messagingException; }).when(mailSender).send(any(MimeMessage.class));

        // Act & Assert
        EmailServiceException exception = assertThrows(EmailServiceException.class, () -> {
            emailService.sendEmailVerification(testUser, token);
        });

        assertNotNull(exception.getCause());
        verify(templateEngine).process(eq("email-verification"), any(Context.class));
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendPasswordResetEmail_Success() throws Exception {
        // Arrange
        String token = "reset-token-123";
        String expectedContent = "<html>Password reset content</html>";
        
        when(templateEngine.process(eq("password-reset"), any(Context.class)))
                .thenReturn(expectedContent);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        // Act
        assertDoesNotThrow(() -> {
            emailService.sendPasswordResetEmail(testUser, token);
        });

        // Assert
        verify(templateEngine).process(eq("password-reset"), any(Context.class));
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendPasswordResetEmail_TemplateEngineException_ShouldThrowEmailServiceException() {
        // Arrange
        String token = "reset-token-123";
        RuntimeException templateException = new RuntimeException("Template processing failed");
        
        when(templateEngine.process(eq("password-reset"), any(Context.class)))
                .thenThrow(templateException);

        // Act & Assert
        EmailServiceException exception = assertThrows(EmailServiceException.class, () -> {
            emailService.sendPasswordResetEmail(testUser, token);
        });

        assertNotNull(exception.getCause());
        verify(templateEngine).process(eq("password-reset"), any(Context.class));
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void sendPasswordResetEmail_MessagingException_ShouldThrowEmailServiceException() throws Exception {
        // Arrange
        String token = "reset-token-123";
        String expectedContent = "<html>Password reset content</html>";
        MessagingException messagingException = new MessagingException("Connection refused");
        
        when(templateEngine.process(eq("password-reset"), any(Context.class)))
                .thenReturn(expectedContent);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doAnswer(invocation -> { throw messagingException; }).when(mailSender).send(any(MimeMessage.class));

        // Act & Assert
        EmailServiceException exception = assertThrows(EmailServiceException.class, () -> {
            emailService.sendPasswordResetEmail(testUser, token);
        });

        assertNotNull(exception.getCause());
        verify(templateEngine).process(eq("password-reset"), any(Context.class));
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendWelcomeEmail_Success() throws Exception {
        // Arrange
        String expectedContent = "<html>Welcome email content</html>";
        
        when(templateEngine.process(eq("welcome"), any(Context.class)))
                .thenReturn(expectedContent);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        // Act
        assertDoesNotThrow(() -> {
            emailService.sendWelcomeEmail(testUser);
        });

        // Assert
        verify(templateEngine).process(eq("welcome"), any(Context.class));
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendWelcomeEmail_TemplateEngineException_ShouldNotThrowException() {
        // Arrange
        RuntimeException templateException = new RuntimeException("Template not found");
        
        when(templateEngine.process(eq("welcome"), any(Context.class)))
                .thenThrow(templateException);

        // Act & Assert - Welcome email failures should not throw exceptions
        assertDoesNotThrow(() -> {
            emailService.sendWelcomeEmail(testUser);
        });

        verify(templateEngine).process(eq("welcome"), any(Context.class));
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void sendWelcomeEmail_MessagingException_ShouldNotThrowException() throws Exception {
        // Arrange
        String expectedContent = "<html>Welcome email content</html>";
        MessagingException messagingException = new MessagingException("SMTP timeout");
        
        when(templateEngine.process(eq("welcome"), any(Context.class)))
                .thenReturn(expectedContent);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doAnswer(invocation -> { throw messagingException; }).when(mailSender).send(any(MimeMessage.class));

        // Act & Assert - Welcome email failures should not throw exceptions
        assertDoesNotThrow(() -> {
            emailService.sendWelcomeEmail(testUser);
        });

        verify(templateEngine).process(eq("welcome"), any(Context.class));
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendEmailVerification_WithNullToken_ShouldWork() throws Exception {
        // Arrange
        String expectedContent = "<html>Email verification content</html>";
        
        when(templateEngine.process(eq("email-verification"), any(Context.class)))
                .thenReturn(expectedContent);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        // Act
        assertDoesNotThrow(() -> {
            emailService.sendEmailVerification(testUser, null);
        });

        // Assert
        verify(templateEngine).process(eq("email-verification"), any(Context.class));
    }

    @Test
    void sendEmailVerification_WithEmptyToken_ShouldWork() throws Exception {
        // Arrange
        String expectedContent = "<html>Email verification content</html>";
        
        when(templateEngine.process(eq("email-verification"), any(Context.class)))
                .thenReturn(expectedContent);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        // Act
        assertDoesNotThrow(() -> {
            emailService.sendEmailVerification(testUser, "");
        });

        // Assert
        verify(templateEngine).process(eq("email-verification"), any(Context.class));
    }

    @Test
    void sendEmailVerification_WithSpecialCharactersInToken_ShouldWork() throws Exception {
        // Arrange
        String specialToken = "token!@#$%^&*()+={}[]|\\:;\"'<>,.?/~`";
        String expectedContent = "<html>Email verification content</html>";
        
        when(templateEngine.process(eq("email-verification"), any(Context.class)))
                .thenReturn(expectedContent);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        // Act
        assertDoesNotThrow(() -> {
            emailService.sendEmailVerification(testUser, specialToken);
        });

        // Assert
        verify(templateEngine).process(eq("email-verification"), any(Context.class));
    }

    @Test
    void contextVariables_ShouldBeSetCorrectly() throws Exception {
        // Arrange
        String token = "test-token";
        String expectedContent = "<html>Content</html>";
        
        when(templateEngine.process(eq("email-verification"), any(Context.class)))
                .thenAnswer(invocation -> {
                    Context context = invocation.getArgument(1);
                    // Verify context variables are set
                    assertTrue(context.containsVariable("user"));
                    assertTrue(context.containsVariable("verificationUrl"));
                    assertEquals(testUser, context.getVariable("user"));
                    String verificationUrl = (String) context.getVariable("verificationUrl");
                    assertTrue(verificationUrl.contains(token));
                    return expectedContent;
                });
        
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        // Act
        emailService.sendEmailVerification(testUser, token);

        // Assert
        verify(templateEngine).process(eq("email-verification"), any(Context.class));
    }

    @Test
    void sendPasswordResetEmail_ContextVariables_ShouldBeSetCorrectly() throws Exception {
        // Arrange
        String token = "reset-token";
        String expectedContent = "<html>Content</html>";
        
        when(templateEngine.process(eq("password-reset"), any(Context.class)))
                .thenAnswer(invocation -> {
                    Context context = invocation.getArgument(1);
                    // Verify context variables are set
                    assertTrue(context.containsVariable("user"));
                    assertTrue(context.containsVariable("resetUrl"));
                    assertEquals(testUser, context.getVariable("user"));
                    String resetUrl = (String) context.getVariable("resetUrl");
                    assertTrue(resetUrl.contains(token));
                    return expectedContent;
                });
        
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        // Act
        emailService.sendPasswordResetEmail(testUser, token);

        // Assert
        verify(templateEngine).process(eq("password-reset"), any(Context.class));
    }

    @Test
    void sendWelcomeEmail_ContextVariables_ShouldBeSetCorrectly() throws Exception {
        // Arrange
        String expectedContent = "<html>Content</html>";
        
        when(templateEngine.process(eq("welcome"), any(Context.class)))
                .thenAnswer(invocation -> {
                    Context context = invocation.getArgument(1);
                    // Verify context variables are set
                    assertTrue(context.containsVariable("user"));
                    assertTrue(context.containsVariable("loginUrl"));
                    assertEquals(testUser, context.getVariable("user"));
                    String loginUrl = (String) context.getVariable("loginUrl");
                    assertTrue(loginUrl.contains("/login"));
                    return expectedContent;
                });
        
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        // Act
        emailService.sendWelcomeEmail(testUser);

        // Assert
        verify(templateEngine).process(eq("welcome"), any(Context.class));
    }

    @Test
    void emailService_WithDifferentFromEmail_ShouldWork() throws Exception {
        // Arrange
        ReflectionTestUtils.setField(emailService, "fromEmail", "different@company.com");
        String expectedContent = "<html>Content</html>";
        
        when(templateEngine.process(eq("email-verification"), any(Context.class)))
                .thenReturn(expectedContent);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        // Act
        assertDoesNotThrow(() -> {
            emailService.sendEmailVerification(testUser, "token");
        });

        // Assert
        verify(templateEngine).process(eq("email-verification"), any(Context.class));
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void emailService_WithDifferentBaseUrl_ShouldWork() throws Exception {
        // Arrange
        ReflectionTestUtils.setField(emailService, "baseUrl", "https://prod.example.com");
        String expectedContent = "<html>Content</html>";
        
        when(templateEngine.process(eq("email-verification"), any(Context.class)))
                .thenReturn(expectedContent);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        // Act
        assertDoesNotThrow(() -> {
            emailService.sendEmailVerification(testUser, "token");
        });

        // Assert
        verify(templateEngine).process(eq("email-verification"), any(Context.class));
        verify(mailSender).send(mimeMessage);
    }
}