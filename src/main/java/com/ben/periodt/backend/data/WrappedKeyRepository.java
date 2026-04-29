package com.ben.periodt.backend.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WrappedKeyRepository extends JpaRepository<WrappedKeyEntity, Long> {
    // JpaRepository provides findById() and save() out of the box!
}