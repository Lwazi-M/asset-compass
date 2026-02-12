package com.assetcompass.tracker.controllers;

import com.assetcompass.tracker.dtos.LoginRequest;
import com.assetcompass.tracker.utils.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final DaoAuthenticationProvider authenticationProvider; // The Bouncer
    private final JwtUtil jwtUtil; // The Badge Printer

    public AuthController(DaoAuthenticationProvider authenticationProvider, JwtUtil jwtUtil) {
        this.authenticationProvider = authenticationProvider;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        // 1. Check Credentials (Email & Password)
        Authentication authentication = authenticationProvider.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        // 2. If correct, set the user as "Logged In"
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. Print the VIP Badge (JWT)
        String jwt = jwtUtil.generateToken(loginRequest.getEmail());

        // 4. Return the Badge to the User
        Map<String, String> response = new HashMap<>();
        response.put("token", jwt);

        return ResponseEntity.ok(response);
    }
}