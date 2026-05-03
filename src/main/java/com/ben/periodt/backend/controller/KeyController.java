package com.ben.periodt.backend.controller;

import com.ben.periodt.backend.data.UserEntity;
import com.ben.periodt.backend.data.UserRepository;
import com.ben.periodt.backend.data.WrappedKeyEntity;
import com.ben.periodt.backend.data.WrappedKeyRepository;
import com.ben.periodt.backend.dto.WrappedKeyDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/v1/keys")
public class KeyController {

    private final WrappedKeyRepository keyRepository;
    private final UserRepository userRepository;

    public KeyController(WrappedKeyRepository keyRepository, UserRepository userRepository) {
        this.keyRepository = keyRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadKey(@Valid @RequestBody WrappedKeyDto request, Authentication authentication) {
        // 1. Get the authenticated user from the JWT token
        UserEntity user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Check if a key already exists for this user
        WrappedKeyEntity keyEntity = keyRepository.findById(user.getId()).orElse(new WrappedKeyEntity());

        // 3. Update the values
        keyEntity.setUserId(user.getId());
        keyEntity.setWrappedDataKey(request.wrappedDataKey());
        keyEntity.setUpdatedAt(OffsetDateTime.now());

        // 4. Save to PostgreSQL
        keyRepository.save(keyEntity);

        return ResponseEntity.ok().build(); // Return 200 OK
    }

    @GetMapping("/fetch")
    public ResponseEntity<?> fetchKey(Authentication authentication) {
        UserEntity user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return keyRepository.findById(user.getId())
                .map(key -> ResponseEntity.ok(new WrappedKeyDto(key.getWrappedDataKey())))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}