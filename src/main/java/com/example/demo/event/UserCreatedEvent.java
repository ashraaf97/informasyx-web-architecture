package com.example.demo.event;

import com.example.demo.domain.Role;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class UserCreatedEvent extends BaseEvent {
    
    private Long userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;
    private String createdBy;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime createdAt;

    public UserCreatedEvent(Long userId, String username, String email, String firstName, 
                           String lastName, Role role, String createdBy, LocalDateTime createdAt) {
        super("USER_CREATED");
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }
}