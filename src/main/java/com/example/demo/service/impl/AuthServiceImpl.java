package com.example.demo.service.impl;

import com.example.demo.domain.User;
import com.example.demo.domain.dto.AuthResponse;
import com.example.demo.domain.dto.LoginRequest;
import com.example.demo.domain.repository.UserRepository;
import com.example.demo.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    
    private final Map<String, String> tokenStore = new ConcurrentHashMap<>();

    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new BadCredentialsException("User not found"));

            if (!user.isActive()) {
                return AuthResponse.failure("User account is deactivated");
            }

            String token = generateSimpleToken(user.getUsername());
            tokenStore.put(token, user.getUsername());
            
            log.info("User {} logged in successfully", user.getUsername());
            return AuthResponse.success(user.getUsername(), token);

        } catch (BadCredentialsException e) {
            log.warn("Failed login attempt for username: {}", loginRequest.getUsername());
            return AuthResponse.failure("Invalid username or password");
        } catch (Exception e) {
            log.error("Login error for username: {}", loginRequest.getUsername(), e);
            return AuthResponse.failure("Login failed");
        }
    }

    @Override
    public AuthResponse logout(String token) {
        try {
            String username = tokenStore.remove(token);
            if (username != null) {
                SecurityContextHolder.clearContext();
                log.info("User {} logged out successfully", username);
                return new AuthResponse(null, username, "Logout successful", true);
            } else {
                return AuthResponse.failure("Invalid token");
            }
        } catch (Exception e) {
            log.error("Logout error", e);
            return AuthResponse.failure("Logout failed");
        }
    }

    private String generateSimpleToken(String username) {
        return "TOKEN_" + username + "_" + System.currentTimeMillis();
    }

    public boolean isValidToken(String token) {
        return tokenStore.containsKey(token);
    }

    public String getUsernameFromToken(String token) {
        return tokenStore.get(token);
    }
}