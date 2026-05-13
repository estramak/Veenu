package com.veenu.veenu_backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank
    private String identifier; // email OR username

    @NotBlank
    private String password;
}