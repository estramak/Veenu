package com.veenu.veenu_backend.dto;

import com.veenu.veenu_backend.model.ListingType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BusinessRequest {

    // Listing fields
    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;

    private String address;

    // Business fields
    @Email
    @NotBlank
    private String email;

    private String phone;
    private String website;
    private String hours;
}