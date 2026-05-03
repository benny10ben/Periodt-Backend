package com.ben.periodt.backend.controller;

import com.ben.periodt.backend.data.UserEntity;
import com.ben.periodt.backend.data.UserRepository;
import com.ben.periodt.backend.data.WrappedKeyEntity;
import com.ben.periodt.backend.data.WrappedKeyRepository;
import com.ben.periodt.backend.dto.*;
import com.ben.periodt.backend.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final WrappedKeyRepository keyRepository; // NEW
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            WrappedKeyRepository keyRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.keyRepository = keyRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username is already taken");
        }

        // 1. Save User with Salt
        UserEntity newUser = new UserEntity();
        newUser.setUsername(request.username());
        newUser.setPasswordHash(passwordEncoder.encode(request.password()));
        newUser.setSalt(request.saltBase64());
        UserEntity savedUser = userRepository.save(newUser);

        // 2. Immediately save the wrapped Data Key so they don't have to hit /api/keys/upload
        WrappedKeyEntity keyEntity = new WrappedKeyEntity();
        keyEntity.setUserId(savedUser.getId());
        keyEntity.setWrappedDataKey(request.wrappedDataKey());
        keyEntity.setUpdatedAt(OffsetDateTime.now());
        keyRepository.save(keyEntity);

        String token = jwtUtil.generateToken(savedUser.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(token, savedUser.getId(), savedUser.getUsername(), savedUser.getSalt()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        UserEntity user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtUtil.generateToken(user.getId());

        // Return the salt so the client can rebuild the Account Key
        return ResponseEntity.ok(new AuthResponse(token, user.getId(), user.getUsername(), user.getSalt()));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request, Authentication authentication) {
        UserEntity user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 1. Verify old password
        if (!passwordEncoder.matches(request.oldPassword(), user.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid old password");
        }

        // 2. Update hash and new salt
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setSalt(request.newSaltBase64());
        userRepository.save(user);

        // 3. Save the newly wrapped Data Key
        WrappedKeyEntity keyEntity = keyRepository.findById(user.getId()).orElse(new WrappedKeyEntity());
        keyEntity.setUserId(user.getId());
        keyEntity.setWrappedDataKey(request.newWrappedDataKey());
        keyEntity.setUpdatedAt(OffsetDateTime.now());
        keyRepository.save(keyEntity);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/change-username")
    public ResponseEntity<?> changeUsername(@Valid @RequestBody ChangeUsernameRequest request, Authentication authentication) {
        UserEntity user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (userRepository.existsByUsername(request.newUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username is already taken");
        }

        user.setUsername(request.newUsername());
        userRepository.save(user);

        String newToken = jwtUtil.generateToken(user.getId());

        return ResponseEntity.ok(new AuthResponse(
                newToken,
                user.getId(),
                user.getUsername(),
                user.getSalt()
        ));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteAccount(Authentication authentication) {
        UserEntity user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.delete(user);
        return ResponseEntity.ok().build();
    }
}