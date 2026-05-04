package com.ben.periodt.backend.dto;

public record AuthResponse(
        String token,
        String refreshToken,
        Long userId,
        String username,
        String saltBase64
) {}