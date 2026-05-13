package com.veenu.veenu_backend.dto;

import com.veenu.veenu_backend.model.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private Long userId;
    private String username;
    private String displayName;
    private UserRole role;
}
