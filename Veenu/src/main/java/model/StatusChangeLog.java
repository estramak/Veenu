package model;

import jakarta.persistence.*;
import lombok.*;
import model.enums.AdminEntityType;
import model.enums.EntityStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "status_change_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class StatusChangeLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdminEntityType entityType;

    @Column(nullable = false)
    private Long entityId;

    @Enumerated(EnumType.STRING)
    private EntityStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EntityStatus newStatus;

    @Column(length = 500)
    private String reason;

    @Column(length = 1000)
    private String adminNotes;

    @Column
    private Long changedBy;

    @Column(nullable = false)
    private LocalDateTime changedAt;

    @PrePersist
    protected void onCreate() {
        changedAt = LocalDateTime.now();
    }
}
