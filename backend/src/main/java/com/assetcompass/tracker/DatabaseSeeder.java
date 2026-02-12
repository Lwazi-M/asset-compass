package com.assetcompass.tracker;

import com.assetcompass.tracker.models.AppUser;
import com.assetcompass.tracker.repositories.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DatabaseSeeder {

    @Bean
    CommandLineRunner initDatabase(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Check if our Admin exists
            if (userRepository.findByEmail("admin@assetcompass.com").isEmpty()) {

                AppUser admin = new AppUser();
                admin.setEmail("admin@assetcompass.com");
                admin.setFullName("Admin User");
                admin.setPassword(passwordEncoder.encode("password123")); // We hash it!
                admin.setRole("ADMIN");

                userRepository.save(admin);
                System.out.println("✅ ADMIN USER CREATED: admin@assetcompass.com / password123");
            } else {
                System.out.println("⚡ Admin user already exists. Skipping creation.");
            }
        };
    }
}