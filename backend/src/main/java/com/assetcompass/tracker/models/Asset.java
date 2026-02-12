package com.assetcompass.tracker.models;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList; // Added
import java.util.List;      // Added

@Entity
@Table(name = "assets")
@Data
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;        // e.g., "BTC", "TSLA", "ETH"

    @Column(nullable = false)
    private String type;        // e.g., "STOCK", "CRYPTO", "REAL_ESTATE"

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal value;   // Precise financial data

    @Column(nullable = false)
    private String currency;    // e.g., "USD", "ZAR"

    private LocalDateTime lastUpdated;

    // RELATIONSHIP: Many Assets belong to One User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    // NEW RELATIONSHIP: One Asset has many Transactions (History)
    @OneToMany(mappedBy = "asset", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> history = new ArrayList<>();

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }
}