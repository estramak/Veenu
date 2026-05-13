package com.veenu.veenu_backend.dto;

import com.veenu.veenu_backend.model.ListingType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ListingRequest {

    @NotBlank
    private String name;

    @NotNull
    private ListingType type;

    private String description;
    private String dateLabel;

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;

    private String address;
}