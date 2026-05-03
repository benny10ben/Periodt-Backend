package com.ben.periodt.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record ChangeUsernameRequest(
        @NotBlank String newUsername
) {}