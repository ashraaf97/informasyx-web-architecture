package com.example.demo.service;

import com.example.demo.domain.User;

public interface EmailService {
    void sendEmailVerification(User user, String token);
    void sendPasswordResetEmail(User user, String token);
    void sendWelcomeEmail(User user);
}