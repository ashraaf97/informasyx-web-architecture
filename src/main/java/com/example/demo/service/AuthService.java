package com.example.demo.service;

import com.example.demo.domain.dto.AuthResponse;
import com.example.demo.domain.dto.LoginRequest;

public interface AuthService {
    AuthResponse login(LoginRequest loginRequest);
    AuthResponse logout(String token);
}