package com.assetcompass.tracker.controllers;

import com.assetcompass.tracker.models.AppUser;
import com.assetcompass.tracker.models.Asset;
import com.assetcompass.tracker.repositories.AppUserRepository;
import com.assetcompass.tracker.repositories.AssetRepository;
import com.assetcompass.tracker.services.CurrencyService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserController {

    private final AppUserRepository userRepository;
    private final AssetRepository assetRepository;
    private final CurrencyService currencyService;

    // Inject repositories and services
    public UserController(AppUserRepository userRepository,
                          AssetRepository assetRepository,
                          CurrencyService currencyService) {
        this.userRepository = userRepository;
        this.assetRepository = assetRepository;
        this.currencyService = currencyService;
    }

    @GetMapping("/me")
    public Map<String, Object> getCurrentUser() {
        // 1. Get Logged-in User
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Calculate Net Worth Dynamically
        List<Asset> assets = assetRepository.findByUserId(user.getId());

        BigDecimal netWorthZAR = BigDecimal.ZERO;
        BigDecimal usdRate = currencyService.getUsdToZarRate(); // Fetch live rate (e.g., 18.25)

        for (Asset asset : assets) {
            // Calculate Asset Value: Quantity * Buy Price
            BigDecimal assetValue = asset.getQuantity().multiply(asset.getBuyPrice());

            // Convert to ZAR if the asset is in USD
            if ("USD".equalsIgnoreCase(asset.getCurrency())) {
                netWorthZAR = netWorthZAR.add(assetValue.multiply(usdRate));
            } else {
                // If it's already ZAR (future feature), just add it
                netWorthZAR = netWorthZAR.add(assetValue);
            }
        }

        // 3. Return User Data + Calculated Net Worth
        return Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "fullName", user.getFullName(),
                "role", user.getRole(),
                "netWorthZAR", netWorthZAR, // This fixes the "R0.00" issue!
                "exchangeRate", usdRate
        );
    }
}