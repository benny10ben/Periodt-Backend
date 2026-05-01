package com.ben.periodt.backend.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    // CHANGED: Spring Data JPA will now write 'SELECT * FROM users WHERE username = ?'
    Optional<UserEntity> findByUsername(String username);

    boolean existsByUsername(String username);
}