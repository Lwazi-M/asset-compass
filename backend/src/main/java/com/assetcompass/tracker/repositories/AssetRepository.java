package com.assetcompass.tracker.repositories;

import com.assetcompass.tracker.models.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AssetRepository extends JpaRepository<Asset, Long> {
    // Find all assets belonging to a specific user
    List<Asset> findByUserId(Long userId);
}