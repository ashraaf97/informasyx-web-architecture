package com.example.demo.controller;

import com.example.demo.domain.Role;
import com.example.demo.domain.dto.AdminCreateUserRequest;
import com.example.demo.domain.dto.AuthResponse;
import com.example.demo.domain.dto.ChangeRoleRequest;
import com.example.demo.security.RequiredRole;
import com.example.demo.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Management", description = "APIs for admin and user management")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/users")
    @Operation(summary = "Create user", description = "Admin and Super Admin can create users")
    @RequiredRole({Role.ADMIN, Role.SUPER_ADMIN})
    public ResponseEntity<AuthResponse> createUser(@Valid @RequestBody AdminCreateUserRequest request) {
        AuthResponse response = adminService.createUser(request);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/users/admin")
    @Operation(summary = "Create admin user", description = "Only Super Admin can create admin users")
    @RequiredRole({Role.SUPER_ADMIN})
    public ResponseEntity<AuthResponse> createAdmin(@Valid @RequestBody AdminCreateUserRequest request) {
        request.setRole(Role.ADMIN);
        AuthResponse response = adminService.createUser(request);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/users/role")
    @Operation(summary = "Change user role", description = "Only Super Admin can change user roles")
    @RequiredRole({Role.SUPER_ADMIN})
    public ResponseEntity<AuthResponse> changeUserRole(@Valid @RequestBody ChangeRoleRequest request) {
        AuthResponse response = adminService.changeUserRole(request);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}