package com.mahendhar.urlshortener.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class ShortUrlTest {

    @Test
    void expirationIncludesTheExactExpirationInstant() {
        Instant now = Instant.now();
        ShortUrl shortUrl = ShortUrl.builder().expiresAt(now).build();

        assertThat(shortUrl.isExpired(now)).isTrue();
        assertThat(shortUrl.isExpired(now.minusSeconds(1))).isFalse();
    }

    @Test
    void lifecycleCallbacksSetAuditFieldsAndActivateNewUrls() {
        ShortUrl shortUrl = new ShortUrl();
        shortUrl.onCreate();

        assertThat(shortUrl.isActive()).isTrue();
        assertThat(shortUrl.getCreatedAt()).isNotNull();
        assertThat(shortUrl.getUpdatedAt()).isNotNull();
    }
}
