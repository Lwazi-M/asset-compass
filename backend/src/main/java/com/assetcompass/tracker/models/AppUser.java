package com.assetcompass.tracker.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity // This tells Hibernate: "Make a table for this!"
@Table(name = "app_users") // We name it 'app_users' because 'user' is a reserved word in Postgres
@Data // Lombok automatically creates Getters, Setters, and toString for us
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password; // This will store the hashed password (e.g., $2a$10$...)

    private String fullName;

    private String role; // e.g., "USER", "ADMIN"
}