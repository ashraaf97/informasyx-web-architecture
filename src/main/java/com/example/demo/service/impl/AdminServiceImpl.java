package com.example.demo.service.impl;

import com.example.demo.domain.Person;
import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import com.example.demo.domain.dto.AdminCreateUserRequest;
import com.example.demo.domain.dto.AuthResponse;
import com.example.demo.domain.dto.ChangeRoleRequest;
import com.example.demo.domain.repository.PersonRepository;
import com.example.demo.domain.repository.UserRepository;
import com.example.demo.event.AdminCreatedEvent;
import com.example.demo.event.UserCreatedEvent;
import com.example.demo.event.UserRoleChangedEvent;
import com.example.demo.security.SecurityUtils;
import com.example.demo.service.AdminService;
import com.example.demo.service.EventPublisherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtils securityUtils;
    private final EventPublisherService eventPublisherService;

    @Override
    public AuthResponse createUser(AdminCreateUserRequest request) {
        try {
            Optional<User> currentUserOpt = securityUtils.getCurrentUser();
            if (currentUserOpt.isEmpty()) {
                return AuthResponse.failure("Unauthorized");
            }

            User currentUser = currentUserOpt.get();
            
            // Validate permissions based on roles
            if (request.getRole() == Role.ADMIN && currentUser.getRole() != Role.SUPER_ADMIN) {
                return AuthResponse.failure("Only Super Admin can create Admin users");
            }
            
            if (request.getRole() == Role.SUPER_ADMIN) {
                return AuthResponse.failure("Cannot create Super Admin users");
            }

            // Check if username already exists
            if (userRepository.findByUsername(request.getUsername()).isPresent()) {
                return AuthResponse.failure("Username already exists");
            }

            // Check if email already exists
            if (personRepository.findByEmail(request.getEmail()).isPresent()) {
                return AuthResponse.failure("Email already exists");
            }

            // Create Person
            Person person = new Person();
            person.setFirstName(request.getFirstName());
            person.setLastName(request.getLastName());
            person.setEmail(request.getEmail());
            person.setPhoneNumber(request.getPhoneNumber());
            person.setAddress(request.getAddress());
            
            Person savedPerson = personRepository.save(person);

            // Create User
            User user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setPerson(savedPerson);
            user.setActive(true);
            user.setEmailVerified(true); // Admin-created users are pre-verified
            user.setRole(request.getRole());

            User savedUser = userRepository.save(user);

            log.info("User {} created with role {} by admin {}", 
                request.getUsername(), request.getRole(), currentUser.getUsername());
            
            // Publish event based on role type
            if (request.getRole() == Role.ADMIN) {
                // Publish admin created event
                AdminCreatedEvent adminEvent = new AdminCreatedEvent(
                    savedUser.getId(),
                    savedUser.getUsername(),
                    savedPerson.getEmail(),
                    savedPerson.getFirstName(),
                    savedPerson.getLastName(),
                    savedUser.getRole(),
                    currentUser.getUsername(),
                    currentUser.getRole().getName(),
                    LocalDateTime.now()
                );
                eventPublisherService.publishAdminCreatedEvent(adminEvent);
            } else {
                // Publish user created event
                UserCreatedEvent userEvent = new UserCreatedEvent(
                    savedUser.getId(),
                    savedUser.getUsername(),
                    savedPerson.getEmail(),
                    savedPerson.getFirstName(),
                    savedPerson.getLastName(),
                    savedUser.getRole(),
                    currentUser.getUsername(),
                    LocalDateTime.now()
                );
                eventPublisherService.publishUserCreatedEvent(userEvent);
            }
            
            return new AuthResponse(null, request.getUsername(), 
                "User created successfully with role " + request.getRole(), true);

        } catch (Exception e) {
            log.error("Error creating user: {}", request.getUsername(), e);
            return AuthResponse.failure("Failed to create user");
        }
    }

    @Override
    public AuthResponse changeUserRole(ChangeRoleRequest request) {
        try {
            Optional<User> currentUserOpt = securityUtils.getCurrentUser();
            if (currentUserOpt.isEmpty()) {
                return AuthResponse.failure("Unauthorized");
            }

            User currentUser = currentUserOpt.get();
            
            // Only super admin can change roles
            if (currentUser.getRole() != Role.SUPER_ADMIN) {
                return AuthResponse.failure("Only Super Admin can change user roles");
            }

            // Cannot change to or from SUPER_ADMIN
            if (request.getRole() == Role.SUPER_ADMIN) {
                return AuthResponse.failure("Cannot promote users to Super Admin");
            }

            Optional<User> targetUserOpt = userRepository.findByUsername(request.getUsername());
            if (targetUserOpt.isEmpty()) {
                return AuthResponse.failure("User not found");
            }

            User targetUser = targetUserOpt.get();
            
            if (targetUser.getRole() == Role.SUPER_ADMIN) {
                return AuthResponse.failure("Cannot change Super Admin role");
            }

            Role oldRole = targetUser.getRole();
            targetUser.setRole(request.getRole());
            User updatedUser = userRepository.save(targetUser);

            log.info("User {} role changed from {} to {} by super admin {}", 
                request.getUsername(), oldRole, request.getRole(), currentUser.getUsername());
            
            // Publish role change event
            UserRoleChangedEvent roleChangeEvent = new UserRoleChangedEvent(
                updatedUser.getId(),
                updatedUser.getUsername(),
                oldRole,
                updatedUser.getRole(),
                currentUser.getUsername(),
                currentUser.getRole().getName(),
                LocalDateTime.now()
            );
            eventPublisherService.publishUserRoleChangedEvent(roleChangeEvent);
            
            return new AuthResponse(null, request.getUsername(), 
                String.format("User role changed from %s to %s", oldRole, request.getRole()), true);

        } catch (Exception e) {
            log.error("Error changing role for user: {}", request.getUsername(), e);
            return AuthResponse.failure("Failed to change user role");
        }
    }
}