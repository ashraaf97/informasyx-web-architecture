package com.example.demo.controller;

import com.example.demo.domain.dto.AuthResponse;
import com.example.demo.domain.dto.LoginRequest;
import com.example.demo.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "APIs for user authentication")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticates user and returns auth token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse response = authService.login(loginRequest);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Logs out user and invalidates token")
    public ResponseEntity<AuthResponse> logout(@RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        AuthResponse response = authService.logout(token);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

}