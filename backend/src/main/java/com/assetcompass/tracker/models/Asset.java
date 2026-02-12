package com.assetcompass.tracker.models;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "assets")
@Data
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;        // e.g., "Apple Stock", "Bitcoin", "Rental Property"

    @Column(nullable = false)
    private String type;        // e.g., "STOCK", "CRYPTO", "REAL_ESTATE", "CASH"

    @Column(nullable = false)
    private BigDecimal value;   // e.g., 1500.50 (Always use BigDecimal for money!)

    @Column(nullable = false)
    private String currency;    // e.g., "USD", "ZAR", "EUR"

    private LocalDateTime lastUpdated;

    // RELATIONSHIP: Many Assets belong to One User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }
}