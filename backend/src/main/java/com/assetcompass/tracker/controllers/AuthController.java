package com.assetcompass.tracker.controllers;

import com.assetcompass.tracker.models.AppUser;
import com.assetcompass.tracker.repositories.AppUserRepository;
import com.assetcompass.tracker.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AppUserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager,
                          AppUserRepository userRepository,
                          JwtUtil jwtUtil,
                          PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    // --- 1. REGISTER (Instant Access) ---
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AppUser user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email is already in use"));
        }

        // Configure User (Unlocked immediately, no verification needed)
        user.setIsEnabled(true);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("USER");

        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "message", "Registration successful! You can now log in."
        ));
    }

    // --- 2. LOGIN ---
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");

        Optional<AppUser> userOpt = userRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            AppUser user = userOpt.get();

            // All users are enabled by default now, but we keep this check for safety
            if (!user.isEnabled()) {
                return ResponseEntity.status(403).body("Account disabled.");
            }

            try {
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
                String token = jwtUtil.generateToken(email);

                return ResponseEntity.ok(Map.of(
                        "token", token,
                        "name", user.getFullName() != null ? user.getFullName() : "User",
                        "role", user.getRole() != null ? user.getRole() : "USER"
                ));
            } catch (Exception e) {
                return ResponseEntity.status(401).body("Invalid credentials");
            }
        }
        return ResponseEntity.status(401).body("Invalid credentials");
    }
}