package com.example.demo.domain.dto;

import com.example.demo.domain.Role;
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
    private Role role;

    public AuthResponse(String token, String username, String message, boolean success) {
        this.token = token;
        this.username = username;
        this.message = message;
        this.success = success;
    }

    public static AuthResponse success(String username, String token, Role role) {
        return new AuthResponse(token, username, "Login successful", true, role);
    }

    public static AuthResponse success(String username, String token) {
        return new AuthResponse(token, username, "Login successful", true);
    }

    public static AuthResponse failure(String message) {
        return new AuthResponse(null, null, message, false, null);
    }
}