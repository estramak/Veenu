package com.veenu.veenu_backend.dto;

import com.veenu.veenu_backend.model.UserRole;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserResponse {
    private Long id;
    private String email;
    private String username;
    private String displayName;
    private String neighborhood;
    private UserRole role;
    private Integer trustScore;
    private LocalDateTime createdAt;
}