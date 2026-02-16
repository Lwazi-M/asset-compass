package com.assetcompass.tracker.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

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
    @Column(nullable = false, precision = 20, scale = 10)
    private BigDecimal quantity;    // e.g., 1.5432000000

    @Column(name = "buy_price", nullable = false, precision = 20, scale = 4)
    private BigDecimal buyPrice;    // e.g., $210.00 (Price in USD)

    // --- NEW: THE "TRADING DESK" UPGRADE ---
    @Column(name = "exchange_rate_at_buy", precision = 20, scale = 4)
    private BigDecimal exchangeRateAtBuy; // e.g., R18.50 (Value of $1 in ZAR when bought)

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
    public BigDecimal calculateCurrentValue(BigDecimal livePrice) {
        if (livePrice == null) return BigDecimal.ZERO;
        return livePrice.multiply(this.quantity);
    }
}