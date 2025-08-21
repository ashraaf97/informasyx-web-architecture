package com.example.demo.domain.dto;

import com.example.demo.domain.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChangeRoleRequest {
    @NotBlank(message = "Username is required")
    private String username;
    
    @NotNull(message = "Role is required")
    private Role role;
}