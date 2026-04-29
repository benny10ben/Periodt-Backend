package com.ben.periodt.backend.data;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "sync_data")
public class SyncDataEntity {

    @Id
    @Column(name = "sync_uuid", updatable = false, nullable = false)
    private UUID syncUuid;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "entity_type", nullable = false)
    private String entityType;

    @Column(name = "encrypted_payload", nullable = false)
    private String encryptedPayload;

    // This is generated automatically by PostgreSQL's BIGSERIAL, so we don't insert/update it from Java.
    @Column(name = "server_version", insertable = false, updatable = false)
    private Long serverVersion;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    // --- Getters and Setters ---
    public UUID getSyncUuid() { return syncUuid; }
    public void setSyncUuid(UUID syncUuid) { this.syncUuid = syncUuid; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public String getEncryptedPayload() { return encryptedPayload; }
    public void setEncryptedPayload(String encryptedPayload) { this.encryptedPayload = encryptedPayload; }

    public Long getServerVersion() { return serverVersion; }

    public Boolean getDeleted() { return isDeleted; }
    public void setDeleted(Boolean deleted) { isDeleted = deleted; }

    public OffsetDateTime getCreatedAt() { return createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}