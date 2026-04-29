package com.ben.periodt.backend.dto;

import java.util.UUID;

public record SyncItemDto(
        UUID syncUuid,
        String entityType,
        String encryptedPayload,
        Boolean isDeleted,
        Long serverVersion // The server fills this in when pulling data
) {}