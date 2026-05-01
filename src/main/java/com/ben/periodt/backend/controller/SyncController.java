package com.ben.periodt.backend.controller;

import com.ben.periodt.backend.data.SyncDataEntity;
import com.ben.periodt.backend.data.SyncDataRepository;
import com.ben.periodt.backend.data.UserEntity;
import com.ben.periodt.backend.data.UserRepository;
import com.ben.periodt.backend.dto.SyncItemDto;
import com.ben.periodt.backend.dto.SyncPullResponse;
import com.ben.periodt.backend.dto.SyncPushRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sync")
public class SyncController {

    private final SyncDataRepository syncRepository;
    private final UserRepository userRepository;

    public SyncController(SyncDataRepository syncRepository, UserRepository userRepository) {
        this.syncRepository = syncRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/push")
    public ResponseEntity<?> pushChanges(@RequestBody SyncPushRequest request, Authentication authentication) {
        UserEntity user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        for (SyncItemDto item : request.items()) {
            // Find existing record
            SyncDataEntity entity = syncRepository.findById(item.syncUuid()).orElse(null);

            if (entity != null) {
                // LAST-WRITE-WINS CONFLICT RESOLUTION
                // If the database already has a newer (or equal) version of this item,
                // reject the stale push from the client.
                if (entity.getClientUpdatedAt() >= item.clientUpdatedAt()) {
                    continue; // Skip this item, move to the next one
                }
            } else {
                // It's a new item, create an empty entity
                entity = new SyncDataEntity();
                entity.setSyncUuid(item.syncUuid());
                entity.setUserId(user.getId());
                entity.setEntityType(item.entityType());
            }

            // Update the entity with the winning client data
            entity.setEncryptedPayload(item.encryptedPayload());
            entity.setDeleted(item.isDeleted() != null ? item.isDeleted() : false);
            entity.setClientUpdatedAt(item.clientUpdatedAt());

            syncRepository.save(entity);
        }

        return ResponseEntity.ok().build();
    }

    @GetMapping("/pull")
    public ResponseEntity<SyncPullResponse> pullChanges(
            @RequestParam(defaultValue = "0") Long cursor,
            Authentication authentication) {

        UserEntity user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Fetch everything newer than the client's cursor
        List<SyncDataEntity> updatedEntities = syncRepository
                .findByUserIdAndServerVersionGreaterThanOrderByServerVersionAsc(user.getId(), cursor);

        // Convert Entities back to DTOs
        List<SyncItemDto> dtoList = updatedEntities.stream()
                .map(e -> new SyncItemDto(
                        e.getSyncUuid(),
                        e.getEntityType(),
                        e.getEncryptedPayload(),
                        e.getDeleted(),
                        e.getServerVersion(),
                        e.getClientUpdatedAt() // Include the timestamp in the pull
                ))
                .collect(Collectors.toList());

        // Calculate the new cursor to hand back to the device
        Long latestCursor = updatedEntities.isEmpty() ?
                cursor : updatedEntities.get(updatedEntities.size() - 1).getServerVersion();

        return ResponseEntity.ok(new SyncPullResponse(dtoList, latestCursor));
    }
}