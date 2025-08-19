package com.example.demo.service.impl;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.*;
import com.example.demo.domain.User;
import com.example.demo.exception.EmailConfigurationException;
import com.example.demo.exception.EmailDeliveryException;
import com.example.demo.exception.EmailException.EmailOperation;
import com.example.demo.exception.EmailServiceException;
import com.example.demo.exception.EmailTemplateException;
import com.example.demo.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.exceptions.TemplateEngineException;

import jakarta.annotation.PostConstruct;
import java.util.Locale;

@Service
@ConditionalOnProperty(name = "app.email.provider", havingValue = "ses")
@Slf4j
public class SesEmailServiceImpl implements EmailService {

    private final TemplateEngine templateEngine;

    @Value("${app.from-email}")
    private String fromEmail;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${aws.ses.access-key}")
    private String accessKey;

    @Value("${aws.ses.secret-key}")
    private String secretKey;

    @Value("${aws.ses.region:us-east-1}")
    private String region;

    private AmazonSimpleEmailService sesClient;

    public SesEmailServiceImpl(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    @PostConstruct
    public void initSesClient() {
        try {
            BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
            
            this.sesClient = AmazonSimpleEmailServiceClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                    .withRegion(Regions.fromName(region))
                    .build();
            
            log.info("AWS SES client initialized for region: {}", region);
        } catch (Exception e) {
            log.error("Failed to initialize AWS SES client", e);
            throw new EmailServiceException(new EmailConfigurationException(
                "Failed to initialize AWS SES client. Check your AWS credentials and region configuration.", 
                "aws.ses", 
                e));
        }
    }

    @Override
    public void sendEmailVerification(User user, String token) {
        try {
            Context context = new Context(Locale.getDefault());
            context.setVariable("user", user);
            context.setVariable("verificationUrl", baseUrl + "/verify-email?token=" + token);
            
            String emailContent = templateEngine.process("email-verification", context);
            
            sendHtmlEmail(
                user.getPerson().getEmail(),
                "Please verify your email address",
                emailContent
            );
            
            log.info("Email verification sent via SES to: {}", user.getPerson().getEmail());
        } catch (TemplateEngineException e) {
            log.error("Failed to process email verification template for: {}", user.getPerson().getEmail(), e);
            throw new EmailServiceException(new EmailTemplateException(
                "Failed to process email verification template", 
                user.getPerson().getEmail(), 
                EmailOperation.EMAIL_VERIFICATION, 
                "email-verification", 
                e));
        } catch (Exception e) {
            log.error("Failed to send email verification via SES to: {}", user.getPerson().getEmail(), e);
            throw new EmailServiceException(new EmailDeliveryException(
                "Failed to send email verification via AWS SES", 
                user.getPerson().getEmail(), 
                EmailOperation.EMAIL_VERIFICATION, 
                EmailDeliveryException.DeliveryFailureReason.AWS_SES_ERROR, 
                e));
        }
    }

    @Override
    public void sendPasswordResetEmail(User user, String token) {
        try {
            Context context = new Context(Locale.getDefault());
            context.setVariable("user", user);
            context.setVariable("resetUrl", baseUrl + "/reset-password?token=" + token);
            
            String emailContent = templateEngine.process("password-reset", context);
            
            sendHtmlEmail(
                user.getPerson().getEmail(),
                "Password Reset Request",
                emailContent
            );
            
            log.info("Password reset email sent via SES to: {}", user.getPerson().getEmail());
        } catch (TemplateEngineException e) {
            log.error("Failed to process password reset template for: {}", user.getPerson().getEmail(), e);
            throw new EmailServiceException(new EmailTemplateException(
                "Failed to process password reset template", 
                user.getPerson().getEmail(), 
                EmailOperation.PASSWORD_RESET, 
                "password-reset", 
                e));
        } catch (Exception e) {
            log.error("Failed to send password reset email via SES to: {}", user.getPerson().getEmail(), e);
            throw new EmailServiceException(new EmailDeliveryException(
                "Failed to send password reset email via AWS SES", 
                user.getPerson().getEmail(), 
                EmailOperation.PASSWORD_RESET, 
                EmailDeliveryException.DeliveryFailureReason.AWS_SES_ERROR, 
                e));
        }
    }

    @Override
    public void sendWelcomeEmail(User user) {
        try {
            Context context = new Context(Locale.getDefault());
            context.setVariable("user", user);
            context.setVariable("loginUrl", baseUrl + "/login");
            
            String emailContent = templateEngine.process("welcome", context);
            
            sendHtmlEmail(
                user.getPerson().getEmail(),
                "Welcome to Informasyx!",
                emailContent
            );
            
            log.info("Welcome email sent via SES to: {}", user.getPerson().getEmail());
        } catch (TemplateEngineException e) {
            log.error("Failed to process welcome email template for: {}", user.getPerson().getEmail(), e);
            // Don't throw exception for welcome email failure - just log
        } catch (Exception e) {
            log.error("Failed to send welcome email via SES to: {}", user.getPerson().getEmail(), e);
            // Don't throw exception for welcome email failure - just log
        }
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            SendEmailRequest request = new SendEmailRequest()
                    .withSource(fromEmail)
                    .withDestination(new Destination().withToAddresses(to))
                    .withMessage(new Message()
                            .withSubject(new Content().withCharset("UTF-8").withData(subject))
                            .withBody(new Body()
                                    .withHtml(new Content().withCharset("UTF-8").withData(htmlContent))
                            )
                    );

            SendEmailResult result = sesClient.sendEmail(request);
            log.debug("Email sent via SES with message ID: {}", result.getMessageId());
            
        } catch (Exception e) {
            log.error("Failed to send email via SES to: {}", to, e);
            throw new EmailServiceException(new EmailDeliveryException(
                "Failed to send HTML email via AWS SES", 
                to, 
                EmailOperation.GENERAL, 
                EmailDeliveryException.DeliveryFailureReason.AWS_SES_ERROR, 
                e));
        }
    }

    private void sendTextEmail(String to, String subject, String textContent) {
        try {
            SendEmailRequest request = new SendEmailRequest()
                    .withSource(fromEmail)
                    .withDestination(new Destination().withToAddresses(to))
                    .withMessage(new Message()
                            .withSubject(new Content().withCharset("UTF-8").withData(subject))
                            .withBody(new Body()
                                    .withText(new Content().withCharset("UTF-8").withData(textContent))
                            )
                    );

            SendEmailResult result = sesClient.sendEmail(request);
            log.debug("Text email sent via SES with message ID: {}", result.getMessageId());
            
        } catch (Exception e) {
            log.error("Failed to send text email via SES to: {}", to, e);
            throw new EmailServiceException(new EmailDeliveryException(
                "Failed to send text email via AWS SES", 
                to, 
                EmailOperation.GENERAL, 
                EmailDeliveryException.DeliveryFailureReason.AWS_SES_ERROR, 
                e));
        }
    }
}