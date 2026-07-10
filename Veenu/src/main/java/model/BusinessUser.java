package model;

import jakarta.persistence.*;
import lombok.Data;
import model.enums.BusinessUserRole;

@Entity
@Data
@Table(
        name = "business_user",
        uniqueConstraints = @UniqueConstraint(columnNames = {"business_id", "user_id"})
        )
public class BusinessUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BusinessUserRole role;
}
