package com.example.demo.service;

import com.example.demo.domain.dto.AdminCreateUserRequest;
import com.example.demo.domain.dto.AuthResponse;
import com.example.demo.domain.dto.ChangeRoleRequest;

public interface AdminService {
    AuthResponse createUser(AdminCreateUserRequest request);
    AuthResponse changeUserRole(ChangeRoleRequest request);
}