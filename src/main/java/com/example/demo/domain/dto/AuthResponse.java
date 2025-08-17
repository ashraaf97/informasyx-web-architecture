package com.example.demo.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String username;
    private String message;
    private boolean success;

    public static AuthResponse success(String username, String token) {
        return new AuthResponse(token, username, "Login successful", true);
    }

    public static AuthResponse failure(String message) {
        return new AuthResponse(null, null, message, false);
    }
}