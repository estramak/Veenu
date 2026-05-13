package com.veenu.veenu_backend.dto;

import com.veenu.veenu_backend.model.ListingStatus;
import com.veenu.veenu_backend.model.ListingType;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ListingResponse {
    private Long id;
    private String name;
    private ListingType type;
    private String description;
    private String dateLabel;
    private Double latitude;
    private Double longitude;
    private String address;
    private ListingStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
