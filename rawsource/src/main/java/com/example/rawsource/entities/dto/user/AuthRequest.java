package com.example.rawsource.entities.dto.user;

import lombok.Data;

@Data
public class AuthRequest {
    private String email;
    private String password;
}