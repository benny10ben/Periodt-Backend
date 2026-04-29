package com.ben.periodt.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record DeviceRegistrationRequest(
        @NotBlank(message = "Device ID is required")
        String deviceId,

        String deviceName
) {}