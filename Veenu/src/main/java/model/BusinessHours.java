package model;

import jakarta.persistence.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Table(name = "business_hours")
public class BusinessHours {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayOfWeek dayOfWeek;  // java.time.DayOfWeek — no need to define your own

    private LocalTime openTime;   // nullable when isClosed = true
    private LocalTime closeTime;

    @Column(nullable = false)
    private boolean isClosed = false;
}
