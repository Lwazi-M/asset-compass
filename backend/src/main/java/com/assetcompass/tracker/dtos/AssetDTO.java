package com.assetcompass.tracker.dtos;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AssetDTO {
    private Long id;
    private String name;
    private String type;
    private BigDecimal value;
    private String currency;
    private LocalDateTime lastUpdated;
}