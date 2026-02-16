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

    // --- NEW: THE "SMART BUY" ENDPOINT ---
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

        // 3. Handle Currency Conversion (ZAR -> USD)
        BigDecimal investedAmountUsd = request.getAmount();

        // If user is paying in ZAR, convert it first
        if ("ZAR".equalsIgnoreCase(request.getCurrency())) {
            BigDecimal usdRate = currencyService.getUsdToZarRate(); // e.g., 18.50
            // Formula: R5000 / 18.50 = $270.27
            investedAmountUsd = request.getAmount().divide(usdRate, 2, RoundingMode.HALF_DOWN);
        }

        // 4. Calculate Shares Purchased
        // Formula: Invested ($270) / Stock Price ($255) = 1.059 shares
        BigDecimal sharesQuantity = investedAmountUsd.divide(stockPriceUsd, 10, RoundingMode.HALF_DOWN);

        // 5. Save to Database
        Asset newAsset = Asset.builder()
                .user(user)
                .name(request.getName())
                .ticker(request.getTicker().toUpperCase())
                .assetType(request.getAssetType())
                .quantity(sharesQuantity)          // Calculated Shares
                .buyPrice(stockPriceUsd)           // Price per share at this moment
                .currency("USD")                   // We store everything in USD for consistency
                .purchaseDate(LocalDateTime.now())
                .lastUpdated(LocalDateTime.now())
                .build();

        assetRepository.save(newAsset);

        return ResponseEntity.ok(Map.of(
                "message", "Asset purchased successfully!",
                "sharesOwned", sharesQuantity,
                "stockPrice", stockPriceUsd,
                "investedUsd", investedAmountUsd
        ));
    }

    // Helper endpoint to get all assets for the user
    @GetMapping
    public List<Asset> getUserAssets() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        AppUser user = userRepository.findByEmail(email).orElseThrow();
        return assetRepository.findByUserId(user.getId());
    }
}