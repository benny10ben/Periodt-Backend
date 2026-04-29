package com.ben.periodt.backend.dto;

import java.util.List;

public record SyncPullResponse(
        List<SyncItemDto> items,
        Long latestCursor
) {}