package model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "business_hours_override")
public class BusinessHoursOverride {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Column(nullable = false)
    private LocalDate overrideDate;

    private LocalTime openTime;
    private LocalTime closeTime;

    @Column(nullable = false)
    private boolean isClosed = false;

    private String label;  // "Christmas Eve", "Staff Training", etc.
}
