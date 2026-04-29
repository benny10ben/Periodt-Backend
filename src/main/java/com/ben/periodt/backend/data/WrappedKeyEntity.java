package com.ben.periodt.backend.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "wrapped_keys")
public class WrappedKeyEntity {

    // The user's ID is the Primary Key because there is only one key per user
    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "wrapped_data_key", nullable = false)
    private String wrappedDataKey;

    @Column(name = "key_version", nullable = false)
    private Integer keyVersion = 1;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    // --- Getters and Setters ---
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getWrappedDataKey() { return wrappedDataKey; }
    public void setWrappedDataKey(String wrappedDataKey) { this.wrappedDataKey = wrappedDataKey; }

    public Integer getKeyVersion() { return keyVersion; }
    public void setKeyVersion(Integer keyVersion) { this.keyVersion = keyVersion; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}