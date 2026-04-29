package com.ben.periodt.backend.controller;

import com.ben.periodt.backend.data.DeviceEntity;
import com.ben.periodt.backend.data.DeviceRepository;
import com.ben.periodt.backend.data.UserEntity;
import com.ben.periodt.backend.data.UserRepository;
import com.ben.periodt.backend.dto.DeviceRegistrationRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;

    public DeviceController(DeviceRepository deviceRepository, UserRepository userRepository) {
        this.deviceRepository = deviceRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerDevice(
            @Valid @RequestBody DeviceRegistrationRequest request,
            Authentication authentication) {

        UserEntity user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Find existing device or create a new one
        DeviceEntity device = deviceRepository.findByUserIdAndDeviceId(user.getId(), request.deviceId())
                .orElse(new DeviceEntity());

        device.setUserId(user.getId());
        device.setDeviceId(request.deviceId());
        device.setDeviceName(request.deviceName());
        device.setLastSeenAt(OffsetDateTime.now());

        deviceRepository.save(device);

        return ResponseEntity.ok().build();
    }
}