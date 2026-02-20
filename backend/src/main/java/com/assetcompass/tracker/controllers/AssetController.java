package com.assetcompass.tracker.controllers;

import com.assetcompass.tracker.dtos.BuyAssetRequest;
import com.assetcompass.tracker.models.AppUser;
import com.assetcompass.tracker.models.Asset;
import com.assetcompass.tracker.models.Transaction;
import com.assetcompass.tracker.repositories.AppUserRepository;
import com.assetcompass.tracker.repositories.AssetRepository;
import com.assetcompass.tracker.repositories.TransactionRepository;
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
    private final TransactionRepository transactionRepository;

    public AssetController(AssetRepository assetRepository,
                           AppUserRepository userRepository,
                           StockService stockService,
                           CurrencyService currencyService,
                           TransactionRepository transactionRepository) {
        this.assetRepository = assetRepository;
        this.userRepository = userRepository;
        this.stockService = stockService;
        this.currencyService = currencyService;
        this.transactionRepository = transactionRepository;
    }

    // --- 1. BUY ASSET ---
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

        // 3. Fetch Live Exchange Rate
        BigDecimal usdRate = currencyService.getUsdToZarRate();

        // 4. Handle Currency Conversion (ZAR -> USD)
        BigDecimal investedAmountUsd = request.getAmount();

        if ("ZAR".equalsIgnoreCase(request.getCurrency())) {
            investedAmountUsd = request.getAmount().divide(usdRate, 2, RoundingMode.HALF_DOWN);
        }

        // 5. Calculate Shares Purchased
        BigDecimal sharesQuantity = investedAmountUsd.divide(stockPriceUsd, 10, RoundingMode.HALF_DOWN);

        // 6. Save to Database
        Asset newAsset = Asset.builder()
                .user(user)
                .name(request.getName())
                .ticker(request.getTicker().toUpperCase())
                .assetType(request.getAssetType())
                .quantity(sharesQuantity)
                .buyPrice(stockPriceUsd)
                .exchangeRateAtBuy(usdRate)
                .currency("USD")
                .purchaseDate(LocalDateTime.now())
                .lastUpdated(LocalDateTime.now())
                .build();

        assetRepository.save(newAsset);

        // --- 7. Create Initial Transaction Log for Graph ---
        Transaction initialLog = new Transaction();
        initialLog.setAsset(newAsset);
        initialLog.setType("BUY");
        initialLog.setValueAtTime(stockPriceUsd.multiply(sharesQuantity));
        transactionRepository.save(initialLog);

        return ResponseEntity.ok(Map.of(
                "message", "Asset purchased successfully!",
                "sharesOwned", sharesQuantity,
                "stockPrice", stockPriceUsd,
                "investedUsd", investedAmountUsd,
                "exchangeRateLocked", usdRate
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

    // --- 3. REFRESH PRICE ---
    @PutMapping("/{id}/refresh")
    public ResponseEntity<?> refreshAssetPrice(@PathVariable Long id) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Asset not found"));

        BigDecimal currentPrice = stockService.getStockPrice(asset.getTicker());

        if (currentPrice != null) {
            BigDecimal oldPrice = asset.getBuyPrice();

            // --- FIX: Update the asset with the new price ---
            asset.setBuyPrice(currentPrice);
            asset.setLastUpdated(LocalDateTime.now());
            assetRepository.save(asset);

            // --- FIX: Log the refresh for the history graph ---
            Transaction refreshLog = new Transaction();
            refreshLog.setAsset(asset);
            refreshLog.setType("PRICE_REFRESH");
            refreshLog.setValueAtTime(currentPrice.multiply(asset.getQuantity()));
            transactionRepository.save(refreshLog);

            return ResponseEntity.ok(Map.of(
                    "ticker", asset.getTicker(),
                    "oldPrice", oldPrice,
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