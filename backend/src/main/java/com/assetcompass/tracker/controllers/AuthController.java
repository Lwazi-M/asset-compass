package com.assetcompass.tracker.controllers;

import com.assetcompass.tracker.dtos.LoginRequest;
import com.assetcompass.tracker.dtos.VerifyRequest;
import com.assetcompass.tracker.services.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    // We only need one dependency now!
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // STEP 1: LOGIN
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            authService.initiateLogin(loginRequest.getEmail(), loginRequest.getPassword());
            return ResponseEntity.ok(Map.of("message", "Verification code sent to email."));
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    // STEP 2: VERIFY
    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody VerifyRequest verifyRequest) {
        try {
            String token = authService.verifyAndLogin(verifyRequest.getEmail(), verifyRequest.getCode());
            return ResponseEntity.ok(Map.of("token", token));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}