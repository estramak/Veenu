package com.veenu.veenu_backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "businesses")
public class Business {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;

    @OneToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false, unique = true)
    private String email;

    private String phone;
    private String website;
    private String hours;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BusinessTier tier = BusinessTier.FREE;

    private Boolean subscriptionActive = false;
    private String stripeCustomerId;
}
