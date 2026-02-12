package com.assetcompass.tracker.controllers;

import com.assetcompass.tracker.dtos.LoginRequest;
import com.assetcompass.tracker.dtos.VerifyRequest;
import com.assetcompass.tracker.models.AppUser;
import com.assetcompass.tracker.repositories.AppUserRepository;
import com.assetcompass.tracker.services.EmailService;
import com.assetcompass.tracker.utils.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final DaoAuthenticationProvider authenticationProvider;
    private final JwtUtil jwtUtil;
    private final AppUserRepository userRepository; // We need this to save the code
    private final EmailService emailService;       // We need this to send the code

    public AuthController(DaoAuthenticationProvider authenticationProvider,
                          JwtUtil jwtUtil,
                          AppUserRepository userRepository,
                          EmailService emailService) {
        this.authenticationProvider = authenticationProvider;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    // STEP 1: LOGIN (Password Check -> Send Code)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        // 1. Check Credentials (Email & Password)
        authenticationProvider.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        // 2. Fetch the user to set the code
        AppUser user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 3. Generate a random 6-digit code (100000 to 999999)
        String verificationCode = String.valueOf((int) (Math.random() * 900000) + 100000);

        // 4. Save it to the database
        user.setVerificationCode(verificationCode);
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15)); // Valid for 15 mins
        userRepository.save(user);

        // 5. "Send" the email
        emailService.sendVerificationEmail(user.getEmail(), verificationCode);

        // 6. Return success message (BUT NO TOKEN YET!) 🛑
        return ResponseEntity.ok(Map.of("message", "Verification code sent to email."));
    }

    // STEP 2: VERIFY (Check Code -> Give Token)
    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody VerifyRequest verifyRequest) {
        // 1. Find the user
        AppUser user = userRepository.findByEmail(verifyRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Check if code exists, matches, and is not expired
        if (user.getVerificationCode() == null ||
                !user.getVerificationCode().equals(verifyRequest.getCode()) ||
                user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {

            return ResponseEntity.badRequest().body(Map.of("error", "Invalid or expired code"));
        }

        // 3. Code is good! Clear it so it can't be used again.
        user.setVerificationCode(null);
        user.setVerificationCodeExpiresAt(null);
        userRepository.save(user);

        // 4. NOW we print the VIP Badge (JWT) 🎟️
        String jwt = jwtUtil.generateToken(user.getEmail());

        return ResponseEntity.ok(Map.of("token", jwt));
    }
}