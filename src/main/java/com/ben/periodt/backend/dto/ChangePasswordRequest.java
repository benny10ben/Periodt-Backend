package com.ben.periodt.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
        @NotBlank String oldPassword,
        @NotBlank String newPassword,
        @NotBlank String newSaltBase64,
        @NotBlank String newWrappedDataKey
) {}