package model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Data;
import model.enums.EntityStatus;
import model.enums.LocationType;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "listing", indexes = {
        @Index(name = "idx_listing_lat", columnList = "latitude"),
        @Index(name = "idx_listing_lon", columnList = "longitude")
        })
public class Listing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    @Size(max = 1000, message = "Description must be under 1000 characters")
    private String description;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(length = 50)
    private String addressLine2;  // suite, unit, floor — nullable by default

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 2)
    private String state;

    @Column(nullable = false, length = 10)
    private String zip;

    @Column(nullable = true, length = 50)
    private String country;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LocationType locationType = LocationType.GENERAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EntityStatus entityStatus = EntityStatus.ACTIVE;

    private Boolean isActive = true;

    private Boolean hasDistinctName = false;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }




}
