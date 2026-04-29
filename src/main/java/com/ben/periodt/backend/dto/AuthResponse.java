package com.ben.periodt.backend.dto;

public record AuthResponse(
        String token,
        Long userId
) {}