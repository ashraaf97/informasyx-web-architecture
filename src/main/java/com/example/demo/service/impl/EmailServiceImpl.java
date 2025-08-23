package com.example.demo.service.impl;

import com.example.demo.domain.User;
import com.example.demo.exception.EmailDeliveryException;
import com.example.demo.exception.EmailException.EmailOperation;
import com.example.demo.exception.EmailServiceException;
import com.example.demo.exception.EmailTemplateException;
import com.example.demo.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.thymeleaf.exceptions.TemplateEngineException;
import java.util.Locale;

@Service
@ConditionalOnProperty(name = "app.email.provider", havingValue = "smtp")
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.from-email}")
    private String fromEmail;

    @Value("${app.base-url}")
    private String baseUrl;

    @Override
    public void sendEmailVerification(User user, String token) {
        try {
            Context context = new Context(Locale.getDefault());
            context.setVariable("user", user);
            context.setVariable("verificationUrl", baseUrl + "/verify-email?token=" + token);
            
            String emailContent = templateEngine.process("email-verification", context);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(user.getPerson().getEmail());
            helper.setFrom(fromEmail);
            helper.setSubject("Please verify your email address");
            helper.setText(emailContent, true);
            
            mailSender.send(message);
            log.info("Email verification sent to: {}", user.getPerson().getEmail());
        } catch (TemplateEngineException e) {
            log.error("Failed to process email verification template for: {}", user.getPerson().getEmail(), e);
            throw new EmailServiceException(new EmailTemplateException(
                "Failed to process email verification template", 
                user.getPerson().getEmail(), 
                EmailOperation.EMAIL_VERIFICATION, 
                "email-verification", 
                e));
        } catch (MessagingException e) {
            log.error("Failed to send email verification to: {}", user.getPerson().getEmail(), e);
            throw new EmailServiceException(new EmailDeliveryException(
                "Failed to send email verification via SMTP", 
                user.getPerson().getEmail(), 
                EmailOperation.EMAIL_VERIFICATION, 
                EmailDeliveryException.DeliveryFailureReason.SMTP_CONNECTION_FAILED, 
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
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(user.getPerson().getEmail());
            helper.setFrom(fromEmail);
            helper.setSubject("Password Reset Request");
            helper.setText(emailContent, true);
            
            mailSender.send(message);
            log.info("Password reset email sent to: {}", user.getPerson().getEmail());
        } catch (TemplateEngineException e) {
            log.error("Failed to process password reset template for: {}", user.getPerson().getEmail(), e);
            throw new EmailServiceException(new EmailTemplateException(
                "Failed to process password reset template", 
                user.getPerson().getEmail(), 
                EmailOperation.PASSWORD_RESET, 
                "password-reset", 
                e));
        } catch (MessagingException e) {
            log.error("Failed to send password reset email to: {}", user.getPerson().getEmail(), e);
            throw new EmailServiceException(new EmailDeliveryException(
                "Failed to send password reset email via SMTP", 
                user.getPerson().getEmail(), 
                EmailOperation.PASSWORD_RESET, 
                EmailDeliveryException.DeliveryFailureReason.SMTP_CONNECTION_FAILED, 
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
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(user.getPerson().getEmail());
            helper.setFrom(fromEmail);
            helper.setSubject("Welcome to Informasyx!");
            helper.setText(emailContent, true);
            
            mailSender.send(message);
            log.info("Welcome email sent to: {}", user.getPerson().getEmail());
        } catch (TemplateEngineException e) {
            log.error("Failed to process welcome email template for: {}", user.getPerson().getEmail(), e);
            // Don't throw exception for welcome email failure - just log
        } catch (MessagingException e) {
            log.error("Failed to send welcome email to: {}", user.getPerson().getEmail(), e);
            // Don't throw exception for welcome email failure - just log
        }
    }

    private void sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            
            mailSender.send(message);
            log.info("Simple email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send simple email to: {}", to, e);
            throw new EmailServiceException(new EmailDeliveryException(
                "Failed to send simple email via SMTP", 
                to, 
                EmailOperation.GENERAL, 
                EmailDeliveryException.DeliveryFailureReason.SMTP_CONNECTION_FAILED, 
                e));
        }
    }
}