package com.ben.periodt.backend.controller;

import com.ben.periodt.backend.data.UserEntity;
import com.ben.periodt.backend.data.UserRepository;
import com.ben.periodt.backend.dto.AuthResponse;
import com.ben.periodt.backend.dto.LoginRequest;
import com.ben.periodt.backend.dto.RegisterRequest;
import com.ben.periodt.backend.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        // 1. Check if user already exists
        if (userRepository.existsByEmail(request.email())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email is already registered");
        }

        // 2. Hash the password and save the new user
        UserEntity newUser = new UserEntity();
        newUser.setEmail(request.email());
        newUser.setPasswordHash(passwordEncoder.encode(request.password()));

        UserEntity savedUser = userRepository.save(newUser);

        // 3. Generate JWT for the new user
        String token = jwtUtil.generateToken(savedUser.getEmail());

        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(token, savedUser.getId()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        // 1. Spring Security handles the password checking securely
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        // 2. If we reach here, the password was correct. Find the user ID.
        UserEntity user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 3. Generate the token
        String token = jwtUtil.generateToken(user.getEmail());

        return ResponseEntity.ok(new AuthResponse(token, user.getId()));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteAccount(Authentication authentication) {
        // 1. Identify the user making the request via their JWT
        UserEntity user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Delete the user (PostgreSQL CASCADE will automatically wipe the devices, keys, and sync_data tables)
        userRepository.delete(user);

        return ResponseEntity.ok().build();
    }
}