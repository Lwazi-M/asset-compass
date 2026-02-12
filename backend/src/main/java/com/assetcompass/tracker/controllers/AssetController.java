package com.assetcompass.tracker.controllers;

import com.assetcompass.tracker.dtos.AssetDTO;
import com.assetcompass.tracker.models.AppUser;
import com.assetcompass.tracker.models.Asset;
import com.assetcompass.tracker.repositories.AppUserRepository;
import com.assetcompass.tracker.repositories.AssetRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/assets")
public class AssetController {

    private final AssetRepository assetRepository;
    private final AppUserRepository userRepository;

    public AssetController(AssetRepository assetRepository, AppUserRepository userRepository) {
        this.assetRepository = assetRepository;
        this.userRepository = userRepository;
    }

    // 1. GET ALL ASSETS (For the logged-in user only)
    @GetMapping
    public List<AssetDTO> getMyAssets() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        AppUser user = userRepository.findByEmail(email).orElseThrow();

        // Find assets for THIS user only
        return assetRepository.findByUserId(user.getId()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 2. ADD A NEW ASSET
    @PostMapping
    public ResponseEntity<AssetDTO> addAsset(@RequestBody AssetDTO assetDTO) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        AppUser user = userRepository.findByEmail(email).orElseThrow();

        // Create the Asset
        Asset asset = new Asset();
        asset.setName(assetDTO.getName());
        asset.setType(assetDTO.getType());
        asset.setValue(assetDTO.getValue());
        asset.setCurrency(assetDTO.getCurrency());
        asset.setUser(user); // Link it to the user!
        asset.setLastUpdated(LocalDateTime.now());

        Asset savedAsset = assetRepository.save(asset);

        return ResponseEntity.ok(convertToDTO(savedAsset));
    }

    // 3. DELETE ASSET
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAsset(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        AppUser user = userRepository.findByEmail(email).orElseThrow();

        // Find the asset
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Asset not found"));

        // SECURITY CHECK: Does this asset belong to the logged-in user?
        if (!asset.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("You do not own this asset");
        }

        assetRepository.delete(asset);
        return ResponseEntity.ok().build();
    }

    // 4. UPDATE ASSET (New!)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAsset(@PathVariable Long id, @RequestBody AssetDTO assetDTO) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        AppUser user = userRepository.findByEmail(email).orElseThrow();

        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Asset not found"));

        // Security Check: Ensure user owns the asset
        if (!asset.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("You do not own this asset");
        }

        // Update Fields
        asset.setName(assetDTO.getName());
        asset.setType(assetDTO.getType());
        asset.setValue(assetDTO.getValue());
        asset.setCurrency(assetDTO.getCurrency());
        asset.setLastUpdated(LocalDateTime.now());

        assetRepository.save(asset);
        return ResponseEntity.ok(convertToDTO(asset));
    }

    // Helper to convert DB Entity -> Clean DTO
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