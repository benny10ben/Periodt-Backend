package com.ben.periodt.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
        @NotBlank String username,
        @NotBlank String password,
        @NotBlank String saltBase64,
        @NotBlank String wrappedDataKey
) {}