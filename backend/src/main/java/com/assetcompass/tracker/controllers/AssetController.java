package com.assetcompass.tracker.controllers;

import com.assetcompass.tracker.dtos.BuyAssetRequest;
import com.assetcompass.tracker.models.AppUser;
import com.assetcompass.tracker.models.Asset;
import com.assetcompass.tracker.repositories.AppUserRepository;
import com.assetcompass.tracker.repositories.AssetRepository;
import com.assetcompass.tracker.services.CurrencyService;
import com.assetcompass.tracker.services.StockService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assets")
public class AssetController {

    private final AssetRepository assetRepository;
    private final AppUserRepository userRepository;
    private final StockService stockService;
    private final CurrencyService currencyService;

    public AssetController(AssetRepository assetRepository,
                           AppUserRepository userRepository,
                           StockService stockService,
                           CurrencyService currencyService) {
        this.assetRepository = assetRepository;
        this.userRepository = userRepository;
        this.stockService = stockService;
        this.currencyService = currencyService;
    }

    // --- 1. BUY ASSET (Updated to Save Exchange Rate) ---
    @PostMapping("/buy")
    public ResponseEntity<?> buyAsset(@RequestBody BuyAssetRequest request) {
        // 1. Get Logged-in User
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AppUser user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Fetch Live Stock Price (in USD)
        BigDecimal stockPriceUsd = stockService.getStockPrice(request.getTicker());
        if (stockPriceUsd == null) {
            return ResponseEntity.badRequest().body("Could not fetch price for ticker: " + request.getTicker());
        }

        // 3. Fetch Live Exchange Rate (CRITICAL for "Trading Desk" features)
        BigDecimal usdRate = currencyService.getUsdToZarRate(); // e.g., 18.50

        // 4. Handle Currency Conversion (ZAR -> USD)
        BigDecimal investedAmountUsd = request.getAmount();

        // If user is paying in ZAR, convert it first
        if ("ZAR".equalsIgnoreCase(request.getCurrency())) {
            // Formula: R5000 / 18.50 = $270.27
            investedAmountUsd = request.getAmount().divide(usdRate, 2, RoundingMode.HALF_DOWN);
        }

        // 5. Calculate Shares Purchased
        // Formula: Invested ($270) / Stock Price ($255) = 1.059 shares
        BigDecimal sharesQuantity = investedAmountUsd.divide(stockPriceUsd, 10, RoundingMode.HALF_DOWN);

        // 6. Save to Database (NOW WITH EXCHANGE RATE HISTORY)
        Asset newAsset = Asset.builder()
                .user(user)
                .name(request.getName())
                .ticker(request.getTicker().toUpperCase())
                .assetType(request.getAssetType())
                .quantity(sharesQuantity)
                .buyPrice(stockPriceUsd)
                .exchangeRateAtBuy(usdRate)        // <--- NEW: Saving the specific rate at this moment!
                .currency("USD")
                .purchaseDate(LocalDateTime.now())
                .lastUpdated(LocalDateTime.now())
                .build();

        assetRepository.save(newAsset);

        return ResponseEntity.ok(Map.of(
                "message", "Asset purchased successfully!",
                "sharesOwned", sharesQuantity,
                "stockPrice", stockPriceUsd,
                "investedUsd", investedAmountUsd,
                "exchangeRateLocked", usdRate // Return this so the user sees the rate used
        ));
    }

    // --- 2. GET ALL ASSETS ---
    @GetMapping
    public List<Asset> getUserAssets() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        AppUser user = userRepository.findByEmail(email).orElseThrow();
        return assetRepository.findByUserId(user.getId());
    }

    // --- 3. REFRESH PRICE (For Profit/Loss Calculation) ---
    @PutMapping("/{id}/refresh")
    public ResponseEntity<?> refreshAssetPrice(@PathVariable Long id) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Asset not found"));

        // Fetch the LATEST price from AlphaVantage
        BigDecimal currentPrice = stockService.getStockPrice(asset.getTicker());

        if (currentPrice != null) {
            // We return the new price so the Frontend can calculate P/L dynamically
            return ResponseEntity.ok(Map.of(
                    "ticker", asset.getTicker(),
                    "oldPrice", asset.getBuyPrice(),
                    "currentPrice", currentPrice,
                    "quantity", asset.getQuantity()
            ));
        }

        return ResponseEntity.badRequest().body("Could not fetch live price.");
    }

    // --- 4. DELETE ASSET ---
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAsset(@PathVariable Long id) {
        assetRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Asset deleted successfully"));
    }
}