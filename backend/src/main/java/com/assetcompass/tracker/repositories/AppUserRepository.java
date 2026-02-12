package com.assetcompass.tracker.repositories;

import com.assetcompass.tracker.models.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// This interface gives us magic methods like .save(), .findAll(), and .delete()
// without us writing ANY SQL code!
public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    // We just declare this method, and Spring automatically writes the SQL for it!
    Optional<AppUser> findByEmail(String email);
}