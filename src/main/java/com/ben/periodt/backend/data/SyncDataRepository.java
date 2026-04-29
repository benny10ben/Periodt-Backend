package com.ben.periodt.backend.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SyncDataRepository extends JpaRepository<SyncDataEntity, UUID> {

    // Automatically generates:
    // SELECT * FROM sync_data WHERE user_id = ? AND server_version > ? ORDER BY server_version ASC
    List<SyncDataEntity> findByUserIdAndServerVersionGreaterThanOrderByServerVersionAsc(Long userId, Long serverVersion);
}