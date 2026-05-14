package com.veenu.veenu_backend.dto;

import lombok.Data;

@Data
public class UserUpdateRequest {
    private String username;
    private String displayName;
    private String neighborhood;
}