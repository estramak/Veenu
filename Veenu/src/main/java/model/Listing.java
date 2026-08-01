package model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import model.enums.EntityStatus;
import model.enums.LocationType;

import java.time.LocalDateTime;

@Entity
@Table(name = "listing", indexes = {
        @Index(name = "idx_listing_lat", columnList = "latitude"),
        @Index(name = "idx_listing_lon", columnList = "longitude")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class Listing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by_user_id", nullable = false)
    private User createdBy;

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

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false)
    private Boolean hasDistinctName = false;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    @Column(length = 500)
    private String suspensionReason;
}
