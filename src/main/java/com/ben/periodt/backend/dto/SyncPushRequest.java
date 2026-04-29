package com.ben.periodt.backend.dto;

import java.util.List;

public record SyncPushRequest(
        List<SyncItemDto> items
) {}