package com.ben.periodt.backend.data;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "refresh_tokens")
public class RefreshTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private OffsetDateTime expiryDate;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public OffsetDateTime getExpiryDate() { return expiryDate; }
    public void setExpiryDate(OffsetDateTime expiryDate) { this.expiryDate = expiryDate; }
}