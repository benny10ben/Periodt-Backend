package com.ben.periodt.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record WrappedKeyDto(
        @NotBlank(message = "Wrapped key cannot be empty")
        String wrappedDataKey
) {}