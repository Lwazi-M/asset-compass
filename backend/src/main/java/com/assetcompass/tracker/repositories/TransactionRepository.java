package com.assetcompass.tracker.repositories;

import com.assetcompass.tracker.models.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    // Find history for a specific asset, newest first
    List<Transaction> findByAssetIdOrderByTimestampDesc(Long assetId);
}