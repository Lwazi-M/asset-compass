package com.assetcompass.tracker.services;

import com.assetcompass.tracker.models.AppUser;
import com.assetcompass.tracker.repositories.AppUserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AppUserRepository userRepository;

    public CustomUserDetailsService(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. Go to the database and look for the user
        AppUser appUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // 2. If found, convert it into a "Security User" (Badge)
        return User.builder()
                .username(appUser.getEmail())
                .password(appUser.getPassword()) // This is the hashed password from DB
                .roles(appUser.getRole())
                .build();
    }
}