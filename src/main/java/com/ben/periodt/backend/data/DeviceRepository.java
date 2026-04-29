package com.ben.periodt.backend.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<DeviceEntity, Long> {

    // Finds a specific device belonging to a specific user
    Optional<DeviceEntity> findByUserIdAndDeviceId(Long userId, String deviceId);
}