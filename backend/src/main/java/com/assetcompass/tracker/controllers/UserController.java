package com.assetcompass.tracker.controllers;

import com.assetcompass.tracker.models.AppUser;
import com.assetcompass.tracker.repositories.AppUserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController // This tells Spring: "I speak JSON, not HTML"
@RequestMapping("/api") // All addresses here start with /api
public class UserController {

    private final AppUserRepository userRepository;

    public UserController(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public com.assetcompass.tracker.dtos.UserDTO getCurrentUser() { // Return type changed to UserDTO
        // 1. Get the logged-in user's email
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        // 2. Get the raw user from DB
        AppUser appUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 3. Convert to Safe DTO (Manual Mapping)
        com.assetcompass.tracker.dtos.UserDTO safeUser = new com.assetcompass.tracker.dtos.UserDTO();
        safeUser.setId(appUser.getId());
        safeUser.setEmail(appUser.getEmail());
        safeUser.setFullName(appUser.getFullName());
        safeUser.setRole(appUser.getRole());

        return safeUser; // Send the safe version!
    }
}