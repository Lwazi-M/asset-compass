package com.assetcompass.tracker.controllers;

import com.assetcompass.tracker.models.AppUser;
import com.assetcompass.tracker.repositories.AppUserRepository;
import com.assetcompass.tracker.security.JwtUtil;
import com.assetcompass.tracker.services.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AppUserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public AuthController(AuthenticationManager authenticationManager,
                          AppUserRepository userRepository,
                          JwtUtil jwtUtil,
                          PasswordEncoder passwordEncoder,
                          EmailService emailService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    // --- 1. REGISTER ---
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AppUser user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email is already in use");
        }

        String code = String.format("%06d", new Random().nextInt(999999));

        user.setVerificationCode(code);
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));
        user.setIsEnabled(false);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("USER");

        userRepository.save(user);

        // Try to send email (it might fail, but we don't care now)
        new Thread(() -> emailService.sendVerificationEmail(user.getEmail(), code)).start();

        // --- DEV MODE: Return code in response ---
        Map<String, String> response = new HashMap<>();
        response.put("message", "Registration successful!");
        response.put("debug_code", code); // <--- THE KEY TO YOUR PROBLEM

        return ResponseEntity.ok(response);
    }

    // --- 2. VERIFY ---
    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");

        Optional<AppUser> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) return ResponseEntity.badRequest().body("User not found");

        AppUser user = userOpt.get();

        if (user.isEnabled()) return ResponseEntity.ok(Map.of("message", "Account already verified."));

        if (user.getVerificationCodeExpiresAt() == null ||
                user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Verification code has expired.");
        }

        if (user.getVerificationCode().equals(code)) {
            user.setEnabled(true);
            user.setVerificationCode(null);
            user.setVerificationCodeExpiresAt(null);
            userRepository.save(user);

            String token = jwtUtil.generateToken(email);
            return ResponseEntity.ok(Map.of("token", token, "message", "Account verified!"));
        }

        return ResponseEntity.badRequest().body("Invalid verification code");
    }

    // --- 3. RESEND CODE ---
    @PostMapping("/resend-code")
    public ResponseEntity<?> resendCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        Optional<AppUser> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) return ResponseEntity.badRequest().body("User not found");

        AppUser user = userOpt.get();

        if (user.isEnabled()) {
            return ResponseEntity.badRequest().body("Account is already verified. Please login.");
        }

        String newCode = String.format("%06d", new Random().nextInt(999999));
        user.setVerificationCode(newCode);
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        new Thread(() -> emailService.sendVerificationEmail(user.getEmail(), newCode)).start();

        // --- DEV MODE: Return code in response ---
        Map<String, String> response = new HashMap<>();
        response.put("message", "New code sent!");
        response.put("debug_code", newCode); // <--- HERE TOO

        return ResponseEntity.ok(response);
    }

    // --- 4. LOGIN ---
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");

        Optional<AppUser> userOpt = userRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            AppUser user = userOpt.get();
            if (!user.isEnabled()) {
                return ResponseEntity.status(403).body("Account not verified.");
            }
            try {
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
                String token = jwtUtil.generateToken(email);
                return ResponseEntity.ok(Map.of("token", token, "name", user.getFullName() != null ? user.getFullName() : "User"));
            } catch (Exception e) {
                return ResponseEntity.status(401).body("Invalid credentials");
            }
        }
        return ResponseEntity.status(401).body("Invalid credentials");
    }
}