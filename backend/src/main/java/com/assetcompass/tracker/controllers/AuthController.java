package com.assetcompass.tracker.controllers;

import com.assetcompass.tracker.dtos.LoginRequest;
import com.assetcompass.tracker.dtos.RegisterRequest;
import com.assetcompass.tracker.dtos.VerifyRequest;
import com.assetcompass.tracker.models.AppUser;
import com.assetcompass.tracker.repositories.AppUserRepository;
import com.assetcompass.tracker.security.JwtUtil;
import com.assetcompass.tracker.services.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    public AuthController(AppUserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil,
                          EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
    }

    // --- 1. REGISTER (Public) ---
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        // A. Check for existing email
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email is already in use.");
        }

        // B. Generate 6-Digit Code
        String verificationCode = String.valueOf(100000 + new Random().nextInt(900000));

        // C. Create User (LOCKED by default)
        AppUser newUser = AppUser.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("USER")
                .verificationCode(verificationCode)
                .isEnabled(false) // <--- Locked until verified
                .build();

        userRepository.save(newUser);

        // D. Send Email Async
        new Thread(() -> {
            emailService.sendVerificationEmail(request.getEmail(), verificationCode);
        }).start();

        return ResponseEntity.ok(Map.of(
                "message", "Registration successful! Please check your email for the verification code."
        ));
    }

    // --- 2. VERIFY ACCOUNT ---
    @PostMapping("/verify")
    public ResponseEntity<?> verifyAccount(@RequestBody VerifyRequest request) {
        AppUser user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isEnabled()) {
            return ResponseEntity.badRequest().body("Account is already verified.");
        }

        if (user.getVerificationCode() != null && user.getVerificationCode().equals(request.getCode())) {
            user.setEnabled(true); // Unlock
            user.setVerificationCode(null); // Cleanup
            userRepository.save(user);
            return ResponseEntity.ok(Map.of("message", "Account verified! You can now login."));
        } else {
            return ResponseEntity.status(400).body("Invalid verification code.");
        }
    }

    // --- 3. LOGIN ---
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        AppUser user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // A. Check if verified
        if (!user.isEnabled()) {
            return ResponseEntity.status(403).body("Account not verified. Please check your email.");
        }

        // B. Authenticate
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            String token = jwtUtil.generateToken(user.getEmail());

            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "name", user.getFullName(),
                    "role", user.getRole()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }
}