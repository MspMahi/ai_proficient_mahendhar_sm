package com.mahendhar.urlshortener.dto;

import java.time.Instant;

public record ShortUrlResponse(
        Long id,
        String originalUrl,
        String shortCode,
        String shortUrl,
        long clickCount,
        boolean active,
        Instant expiresAt,
        Instant createdAt,
        Instant updatedAt
) {
}

