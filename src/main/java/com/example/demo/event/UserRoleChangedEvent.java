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
public class UserRoleChangedEvent extends BaseEvent {
    
    private Long userId;
    private String username;
    private Role previousRole;
    private Role newRole;
    private String changedBy;
    private String changedByRole;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime changedAt;

    public UserRoleChangedEvent(Long userId, String username, Role previousRole, Role newRole, 
                               String changedBy, String changedByRole, LocalDateTime changedAt) {
        super("USER_ROLE_CHANGED");
        this.userId = userId;
        this.username = username;
        this.previousRole = previousRole;
        this.newRole = newRole;
        this.changedBy = changedBy;
        this.changedByRole = changedByRole;
        this.changedAt = changedAt;
    }
}