package com.assetcompass.tracker.dtos;

import lombok.Data;

@Data // Auto-generates getters/setters
public class UserDTO {
    private Long id;
    private String email;
    private String fullName;
    private String role;

    // Notice: NO PASSWORD field here! 🚫 key
}