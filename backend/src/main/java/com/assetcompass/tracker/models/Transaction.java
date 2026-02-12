package com.assetcompass.tracker.models;

import com.fasterxml.jackson.annotation.JsonIgnore; // Import this
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal valueAtTime;

    @Column(nullable = false)
    private String type;

    private LocalDateTime timestamp;

    @JsonIgnore // <--- This prevents the infinite loop/fetch error
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}