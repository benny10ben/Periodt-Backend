package com.ben.periodt.backend.controller;

import com.ben.periodt.backend.data.*;
import com.ben.periodt.backend.dto.*;
import com.ben.periodt.backend.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final WrappedKeyRepository keyRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            WrappedKeyRepository keyRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.keyRepository = keyRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    private String createAndSaveRefreshToken(Long userId) {
        // Multi-device fix: We no longer delete old tokens here.
        String refreshToken = jwtUtil.generateRefreshToken(userId);
        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.setToken(refreshToken);
        entity.setUserId(userId);
        entity.setExpiryDate(OffsetDateTime.now().plusDays(30));
        refreshTokenRepository.save(entity);
        return refreshToken;
    }

    @PostMapping("/register")
    @Transactional
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Username taken"));
        }

        UserEntity newUser = new UserEntity();
        newUser.setUsername(request.username());
        newUser.setPasswordHash(passwordEncoder.encode(request.password()));
        newUser.setSalt(request.saltBase64());
        UserEntity savedUser = userRepository.save(newUser);

        WrappedKeyEntity keyEntity = new WrappedKeyEntity();
        keyEntity.setUserId(savedUser.getId());
        keyEntity.setWrappedDataKey(request.wrappedDataKey());
        keyEntity.setUpdatedAt(OffsetDateTime.now());
        keyRepository.save(keyEntity);

        String accessToken = jwtUtil.generateAccessToken(savedUser.getId(), savedUser.getUsername());
        String refreshToken = createAndSaveRefreshToken(savedUser.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(accessToken, refreshToken, savedUser.getId(), savedUser.getUsername(), savedUser.getSalt()));
    }

    @PostMapping("/login")
    @Transactional
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        UserEntity user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername());
        String refreshToken = createAndSaveRefreshToken(user.getId());

        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken, user.getId(), user.getUsername(), user.getSalt()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        String requestRefreshToken = request.get("refreshToken");
        if (requestRefreshToken == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing token"));
        }

        Optional<RefreshTokenEntity> tokenOpt = refreshTokenRepository.findByToken(requestRefreshToken);
        if (tokenOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid token"));
        }

        RefreshTokenEntity tokenEntity = tokenOpt.get();
        if (tokenEntity.getExpiryDate().isBefore(OffsetDateTime.now())) {
            refreshTokenRepository.delete(tokenEntity);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Expired token"));
        }

        return userRepository.findById(tokenEntity.getUserId())
                .map(user -> {
                    String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername());
                    return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User not found")));
    }

    @PostMapping("/logout")
    @Transactional
    public ResponseEntity<?> logout(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        if (refreshToken != null) {
            refreshTokenRepository.deleteByToken(refreshToken);
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/change-password")
    @Transactional
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request, Authentication authentication) {
        UserEntity user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.oldPassword(), user.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid old password"));
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setSalt(request.newSaltBase64());
        userRepository.save(user);

        WrappedKeyEntity keyEntity = keyRepository.findById(user.getId()).orElse(new WrappedKeyEntity());
        keyEntity.setUserId(user.getId());
        keyEntity.setWrappedDataKey(request.newWrappedDataKey());
        keyEntity.setUpdatedAt(OffsetDateTime.now());
        keyRepository.save(keyEntity);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/change-username")
    @Transactional
    public ResponseEntity<?> changeUsername(@Valid @RequestBody ChangeUsernameRequest request, Authentication authentication) {
        UserEntity user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (userRepository.existsByUsername(request.newUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Username taken"));
        }

        user.setUsername(request.newUsername());
        userRepository.save(user);

        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername());
        String newRefreshToken = createAndSaveRefreshToken(user.getId());

        return ResponseEntity.ok(new AuthResponse(newAccessToken, newRefreshToken, user.getId(), user.getUsername(), user.getSalt()));
    }

    @DeleteMapping("/delete")
    @Transactional
    public ResponseEntity<?> deleteAccount(Authentication authentication) {
        UserEntity user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        refreshTokenRepository.deleteByUserId(user.getId());
        userRepository.delete(user);
        return ResponseEntity.ok().build();
    }
}