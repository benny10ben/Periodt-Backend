package com.ben.periodt.backend.data;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {
    Optional<RefreshTokenEntity> findByToken(String token);
    void deleteByUserId(Long userId);
    void deleteByToken(String token);
}