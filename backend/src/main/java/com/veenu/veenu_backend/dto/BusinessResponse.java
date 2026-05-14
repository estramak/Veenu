package com.veenu.veenu_backend.dto;

import com.veenu.veenu_backend.model.BusinessTier;
import com.veenu.veenu_backend.model.ListingStatus;
import lombok.Data;

@Data
public class BusinessResponse {
    private Long id;
    private Long listingId;
    private String name;
    private String description;
    private Double latitude;
    private Double longitude;
    private String address;
    private String email;
    private String phone;
    private String website;
    private String hours;
    private BusinessTier tier;
    private Boolean subscriptionActive;
    private ListingStatus listingStatus;
}