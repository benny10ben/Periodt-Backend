package com.ben.periodt.backend.data;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "devices", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "device_id"})
})
public class DeviceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "device_id", nullable = false)
    private String deviceId; // Unique hardware ID from Android

    @Column(name = "device_name")
    private String deviceName; // e.g., "Pixel 8 Pro"

    @Column(name = "last_cursor", nullable = false)
    private Long lastCursor = 0L;

    @Column(name = "registered_at", nullable = false, updatable = false)
    private OffsetDateTime registeredAt;

    @Column(name = "last_seen_at", nullable = false)
    private OffsetDateTime lastSeenAt;

    @PrePersist
    protected void onCreate() {
        registeredAt = OffsetDateTime.now();
        lastSeenAt = registeredAt;
    }

    @PreUpdate
    protected void onUpdate() {
        lastSeenAt = OffsetDateTime.now();
    }

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

    public Long getLastCursor() { return lastCursor; }
    public void setLastCursor(Long lastCursor) { this.lastCursor = lastCursor; }

    public OffsetDateTime getRegisteredAt() { return registeredAt; }
    public OffsetDateTime getLastSeenAt() { return lastSeenAt; }
    public void setLastSeenAt(OffsetDateTime lastSeenAt) { this.lastSeenAt = lastSeenAt; }
}