package model;

import jakarta.persistence.*;
import model.enums.EntityStatus;
import model.enums.UserRole;
import repositories.BusinessRepository;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "business")
public class Business {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = true, unique = true)
    private String phone;

    @Column(nullable = true, unique = true)
    private String website;

    //separate tables, cover regular business hours, and when they change due to holidays or one off changes
    @OneToMany(mappedBy = "business", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BusinessHours> hours = new ArrayList<>();
    @OneToMany(mappedBy = "business", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BusinessHoursOverride> hoursOverrides = new ArrayList<>();


    @OneToMany(mappedBy = "business", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BusinessUser> businessUsers = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EntityStatus entityStatus = EntityStatus.ACTIVE;

    @Column(nullable = false)
    private Boolean isVerified = false;

    @Column(nullable = false)
    private String suspensionReason;
}
