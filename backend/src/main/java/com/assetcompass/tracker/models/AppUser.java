package com.assetcompass.tracker.models;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Entity
@Table(name = "app_users") // Kept as 'app_users' to avoid Postgres reserved word conflicts
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppUser implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String fullName;

    private String role; // e.g., "USER", "ADMIN"

    // --- NEW: SECURE VERIFICATION FIELDS ---
    @Column(name = "verification_code")
    private String verificationCode; // Stores the 6-digit code

    @Column(name = "is_enabled")
    private boolean isEnabled = false; // Account is LOCKED by default until verified

    // --- UserDetails Implementation (Required by Spring Security) ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Converts "USER" role into a Spring Security Authority
        if (role == null) return Collections.emptyList();
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getUsername() {
        return email; // We use email as the username
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // We could use this to lock bad actors later
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled; // This connects to the database column!
    }
}