package model;

import jakarta.persistence.*;
import lombok.Data;
import model.enums.EntityStatus;
import model.enums.UserRole;
import model.enums.EntityStatus;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false, unique = true)
    private String email;

    private Boolean emailVerified = false;

    @Column(nullable = false)
    private Integer trustScore = 0;

    private Boolean isActive = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EntityStatus entityStatus = EntityStatus.ACTIVE;

    @Column(nullable = false)
    private String suspensionReason;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    @Column(nullable = false)
    private String displayName;

    private String neighborhood;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.COMMUNITY;
}
