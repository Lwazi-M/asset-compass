package com.assetcompass.tracker.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.math.BigDecimal; // Import for precision math

@Entity
@Table(name = "assets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // -- IDENTITY --
    @Column(nullable = false)
    private String name;        // e.g., "Apple Inc."

    @Column(nullable = false)
    private String ticker;      // e.g., "AAPL"

    @Column(name = "asset_type", nullable = false)
    private String assetType;   // e.g., "STOCK", "ETF", "CRYPTO"

    // -- FINANCIALS --
    // We use BigDecimal for money/shares because 'double' causes math errors (0.1 + 0.2 = 0.30000004)
    @Column(nullable = false, precision = 20, scale = 10)
    private BigDecimal quantity;    // e.g., 1.5432000000

    @Column(name = "buy_price", nullable = false, precision = 20, scale = 2)
    private BigDecimal buyPrice;    // e.g., $210.00

    @Column(nullable = false)
    private String currency;    // "USD" or "ZAR"

    // -- METADATA --
    @Column(name = "purchase_date")
    private LocalDateTime purchaseDate;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    // Helper to calculate current Total Value (Live Price * Quantity)
    // We will use this in the Controller later
    public BigDecimal calculateCurrentValue(BigDecimal livePrice) {
        return livePrice.multiply(this.quantity);
    }
}