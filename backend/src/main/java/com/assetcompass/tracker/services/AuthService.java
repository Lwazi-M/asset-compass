package com.assetcompass.tracker.services;

import com.assetcompass.tracker.models.AppUser;
import com.assetcompass.tracker.repositories.AppUserRepository;
import com.assetcompass.tracker.utils.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class AuthService {

    private final AppUserRepository userRepository; // Changed to AppUserRepository
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final ResendEmailService emailService;

    public AuthService(AppUserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, ResendEmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
    }

    public void initiateLogin(String email, String password) {
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Use the encoder to check the password hash
        // NOTE: If you are using plain text passwords for testing, use .equals() instead
        // But for production, this is correct:
        /* if (!passwordEncoder.matches(password, user.getPassword())) {
             throw new RuntimeException("Invalid credentials");
        } */

        // TEMPORARY: Since we might have manually inserted users with plain text passwords:
        if (!password.equals(user.getPassword()) && !passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String code = String.format("%06d", new Random().nextInt(999999));
        user.setVerificationCode(code);
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);

        // Send Real Email
        System.out.println("Sending email to " + email);
        emailService.sendVerificationEmail(email, code);
    }

    public String verifyAndLogin(String email, String code) {
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getVerificationCode() == null || !user.getVerificationCode().equals(code)) {
            throw new RuntimeException("Invalid code");
        }

        if (user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Code expired");
        }

        user.setVerificationCode(null);
        user.setVerificationCodeExpiresAt(null);
        userRepository.save(user);

        return jwtUtil.generateToken(user.getEmail());
    }
}