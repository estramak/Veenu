package model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "event")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;

    @Column(name = "business_id")
    private Long businessId;

    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;

    private Boolean isFree = false;
    private BigDecimal price = BigDecimal.ZERO;

    private Integer capacity;
    private String registrationUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;
}
