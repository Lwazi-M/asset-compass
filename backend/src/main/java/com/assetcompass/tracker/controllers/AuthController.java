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

    // --- 1. REGISTER (Public) ---
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AppUser user) {
        // A. Check for existing email
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email is already in use");
        }

        // B. Generate 6-Digit Code
        String code = String.format("%06d", new Random().nextInt(999999));

        // --- FIX: Set Code AND Expiry Date (15 mins) ---
        user.setVerificationCode(code);
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));

        // C. Configure User (Locked by default)
        user.setIsEnabled(false);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("USER"); // Default role

        userRepository.save(user);

        // D. Send Email (with Debug Logs)
        new Thread(() -> {
            try {
                System.out.println("--- EMAIL START ---");
                System.out.println("Sending code " + code + " to " + user.getEmail());
                emailService.sendVerificationEmail(user.getEmail(), code);
                System.out.println("--- EMAIL SUCCESS ---");
            } catch (Exception e) {
                System.err.println("--- EMAIL FAILED ---");
                System.err.println(e.getMessage());
                e.printStackTrace();
            }
        }).start();

        return ResponseEntity.ok(Map.of(
                "message", "User registered successfully. Please check your email for the code."
        ));
    }

    // --- 2. VERIFY ACCOUNT ---
    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");

        Optional<AppUser> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found");
        }

        AppUser user = userOpt.get();

        // Check if already verified
        if (user.isEnabled()) {
            return ResponseEntity.ok(Map.of("message", "Account is already verified."));
        }

        // --- FIX: Check Expiry Date ---
        if (user.getVerificationCodeExpiresAt() == null ||
                user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Verification code has expired. Please request a new one.");
        }

        if (user.getVerificationCode().equals(code)) {
            user.setEnabled(true); // Unlock user
            user.setVerificationCode(null);
            user.setVerificationCodeExpiresAt(null);
            userRepository.save(user);

            // Auto-login after verification
            String token = jwtUtil.generateToken(email);
            return ResponseEntity.ok(Map.of("token", token, "message", "Account verified!"));
        }

        return ResponseEntity.badRequest().body("Invalid verification code");
    }

    // --- 3. RESEND CODE (NEW ENDPOINT) ---
    @PostMapping("/resend-code")
    public ResponseEntity<?> resendCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        Optional<AppUser> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found");
        }

        AppUser user = userOpt.get();

        if (user.isEnabled()) {
            return ResponseEntity.badRequest().body("Account is already verified. Please login.");
        }

        // Generate NEW code
        String newCode = String.format("%06d", new Random().nextInt(999999));
        user.setVerificationCode(newCode);
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15)); // Reset expiry
        userRepository.save(user);

        // Send Email
        new Thread(() -> {
            try {
                System.out.println("--- RESENDING EMAIL ---");
                emailService.sendVerificationEmail(user.getEmail(), newCode);
            } catch (Exception e) {
                System.err.println("--- RESEND FAILED ---");
                e.printStackTrace();
            }
        }).start();

        return ResponseEntity.ok(Map.of("message", "New verification code sent!"));
    }

    // --- 4. LOGIN ---
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");

        Optional<AppUser> userOpt = userRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            AppUser user = userOpt.get();

            // A. Check if verified
            if (!user.isEnabled()) {
                return ResponseEntity.status(403).body("Account not verified. Please check your email.");
            }

            // B. Authenticate
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