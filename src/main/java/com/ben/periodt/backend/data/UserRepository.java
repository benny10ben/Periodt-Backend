package com.ben.periodt.backend.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    // Spring Data JPA automatically writes the SQL query for this!
    // SELECT * FROM users WHERE email = ?
    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);
}