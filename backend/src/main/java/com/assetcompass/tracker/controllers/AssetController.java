package com.assetcompass.tracker.controllers;

import com.assetcompass.tracker.dtos.AssetDTO;
import com.assetcompass.tracker.models.AppUser;
import com.assetcompass.tracker.models.Asset;
import com.assetcompass.tracker.models.Transaction;
import com.assetcompass.tracker.repositories.AppUserRepository;
import com.assetcompass.tracker.repositories.AssetRepository;
import com.assetcompass.tracker.repositories.TransactionRepository;
import com.assetcompass.tracker.services.PriceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/assets")
public class AssetController {

    private final AssetRepository assetRepository;
    private final AppUserRepository userRepository;
    private final PriceService priceService;
    private final TransactionRepository transactionRepository; // Added

    public AssetController(AssetRepository assetRepository,
                           AppUserRepository userRepository,
                           PriceService priceService,
                           TransactionRepository transactionRepository) { // Updated constructor
        this.assetRepository = assetRepository;
        this.userRepository = userRepository;
        this.priceService = priceService;
        this.transactionRepository = transactionRepository;
    }

    @GetMapping
    public List<AssetDTO> getMyAssets() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        AppUser user = userRepository.findByEmail(email).orElseThrow();

        return assetRepository.findByUserId(user.getId()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<AssetDTO> addAsset(@RequestBody AssetDTO assetDTO) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        AppUser user = userRepository.findByEmail(email).orElseThrow();

        Asset asset = new Asset();
        asset.setName(assetDTO.getName());
        asset.setType(assetDTO.getType());
        asset.setValue(assetDTO.getValue());
        asset.setCurrency(assetDTO.getCurrency());
        asset.setUser(user);
        asset.setLastUpdated(LocalDateTime.now());

        Asset savedAsset = assetRepository.save(asset);

        // Log initial transaction
        logTransaction(savedAsset, assetDTO.getValue(), "INITIAL_DEPOSIT");

        return ResponseEntity.ok(convertToDTO(savedAsset));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAsset(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        AppUser user = userRepository.findByEmail(email).orElseThrow();

        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Asset not found"));

        if (!asset.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("You do not own this asset");
        }

        assetRepository.delete(asset);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAsset(@PathVariable Long id, @RequestBody AssetDTO assetDTO) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        AppUser user = userRepository.findByEmail(email).orElseThrow();

        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Asset not found"));

        if (!asset.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("You do not own this asset");
        }

        asset.setName(assetDTO.getName());
        asset.setType(assetDTO.getType());
        asset.setValue(assetDTO.getValue());
        asset.setCurrency(assetDTO.getCurrency());
        asset.setLastUpdated(LocalDateTime.now());

        assetRepository.save(asset);

        // Log manual update as a transaction
        logTransaction(asset, assetDTO.getValue(), "MANUAL_UPDATE");

        return ResponseEntity.ok(convertToDTO(asset));
    }

    @PostMapping("/{id}/refresh")
    public ResponseEntity<?> refreshAssetPrice(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        AppUser user = userRepository.findByEmail(email).orElseThrow();

        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Asset not found"));

        if (!asset.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }

        String symbol = asset.getName().split(" ")[0];
        BigDecimal newPrice = priceService.fetchPrice(symbol);

        if (newPrice.compareTo(BigDecimal.ZERO) > 0) {
            asset.setValue(newPrice);
            asset.setLastUpdated(LocalDateTime.now());
            assetRepository.save(asset);

            // Log automatic refresh as a transaction
            logTransaction(asset, newPrice, "PRICE_REFRESH");

            return ResponseEntity.ok(convertToDTO(asset));
        } else {
            return ResponseEntity.badRequest().body("Could not fetch price. Ensure asset name starts with a valid ticker symbol.");
        }
    }

    // 6. GET ASSET HISTORY
    @GetMapping("/{id}/history")
    public ResponseEntity<List<Transaction>> getAssetHistory(@PathVariable Long id) {
        return ResponseEntity.ok(transactionRepository.findByAssetIdOrderByTimestampDesc(id));
    }

    // Private helper to log transactions
    private void logTransaction(Asset asset, BigDecimal value, String type) {
        Transaction transaction = new Transaction();
        transaction.setAsset(asset);
        transaction.setValueAtTime(value);
        transaction.setType(type);
        transactionRepository.save(transaction);
    }

    private AssetDTO convertToDTO(Asset asset) {
        AssetDTO dto = new AssetDTO();
        dto.setId(asset.getId());
        dto.setName(asset.getName());
        dto.setType(asset.getType());
        dto.setValue(asset.getValue());
        dto.setCurrency(asset.getCurrency());
        dto.setLastUpdated(asset.getLastUpdated());
        return dto;
    }
}