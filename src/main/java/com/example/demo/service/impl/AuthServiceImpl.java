package com.example.demo.service.impl;

import com.example.demo.domain.Person;
import com.example.demo.domain.User;
import com.example.demo.domain.dto.AuthResponse;
import com.example.demo.domain.dto.ChangePasswordRequest;
import com.example.demo.domain.dto.LoginRequest;
import com.example.demo.domain.dto.SignUpRequest;
import com.example.demo.domain.repository.PersonRepository;
import com.example.demo.domain.repository.UserRepository;
import com.example.demo.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final PasswordEncoder passwordEncoder;
    
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

    @Override
    public AuthResponse changePassword(String token, ChangePasswordRequest changePasswordRequest) {
        try {
            String username = getUsernameFromToken(token);
            if (username == null) {
                return AuthResponse.failure("Invalid or expired token");
            }

            if (!changePasswordRequest.getNewPassword().equals(changePasswordRequest.getConfirmPassword())) {
                return AuthResponse.failure("New password and confirm password do not match");
            }

            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("User not found"));

            if (!user.isActive()) {
                return AuthResponse.failure("User account is deactivated");
            }

            if (!passwordEncoder.matches(changePasswordRequest.getCurrentPassword(), user.getPassword())) {
                log.warn("Invalid current password attempt for user: {}", username);
                return AuthResponse.failure("Current password is incorrect");
            }

            String encodedNewPassword = passwordEncoder.encode(changePasswordRequest.getNewPassword());
            user.setPassword(encodedNewPassword);
            userRepository.save(user);

            log.info("Password changed successfully for user: {}", username);
            return new AuthResponse(null, username, "Password changed successfully", true);

        } catch (Exception e) {
            log.error("Change password error", e);
            return AuthResponse.failure("Failed to change password");
        }
    }

    @Override
    public AuthResponse signUp(SignUpRequest signUpRequest) {
        try {
            if (!signUpRequest.getPassword().equals(signUpRequest.getConfirmPassword())) {
                return AuthResponse.failure("Password and confirm password do not match");
            }

            if (userRepository.findByUsername(signUpRequest.getUsername()).isPresent()) {
                return AuthResponse.failure("Username already exists");
            }

            if (personRepository.findByEmail(signUpRequest.getEmail()).isPresent()) {
                return AuthResponse.failure("Email already exists");
            }

            Person person = new Person();
            person.setFirstName(signUpRequest.getFirstName());
            person.setLastName(signUpRequest.getLastName());
            person.setEmail(signUpRequest.getEmail());
            person.setPhoneNumber(signUpRequest.getPhoneNumber());
            person.setAddress(signUpRequest.getAddress());
            
            Person savedPerson = personRepository.save(person);

            User user = new User();
            user.setUsername(signUpRequest.getUsername());
            user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
            user.setPerson(savedPerson);
            user.setActive(true);
            user.setRoles("USER");

            userRepository.save(user);

            log.info("New user registered: {}", signUpRequest.getUsername());
            return new AuthResponse(null, signUpRequest.getUsername(), "User registered successfully", true);

        } catch (Exception e) {
            log.error("Sign up error for username: {}", signUpRequest.getUsername(), e);
            return AuthResponse.failure("Registration failed");
        }
    }
}