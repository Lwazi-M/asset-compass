package com.assetcompass.tracker.dtos;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class BuyAssetRequest {
    private String ticker;        // e.g., "AAPL"
    private String name;          // e.g., "Apple Inc"
    private String assetType;     // e.g., "STOCK"
    private BigDecimal amount;    // e.g., 5000 (The money you are investing)
    private String currency;      // e.g., "ZAR" or "USD"
}