package com.assetcompass.tracker.models;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

@Entity
@Table(name = "app_users")
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

    // --- VERIFICATION FIELDS ---
    @Column(name = "verification_code")
    private String verificationCode;

    @Column(name = "verification_expires_at")
    private LocalDateTime verificationCodeExpiresAt;

    // We use Boolean (wrapper) to allow NULL values from old database records
    @Builder.Default
    @Column(name = "is_enabled")
    private Boolean isEnabled = false;

    // --- FIX: Manual Setter to support AuthController calling setEnabled(true) ---
    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
    }

    // --- UserDetails Implementation ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (role == null) return Collections.emptyList();
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() {
        // Safely handle nulls from DB
        return Boolean.TRUE.equals(isEnabled);
    }
}