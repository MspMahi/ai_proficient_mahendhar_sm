package com.mahendhar.urlshortener.service;

import com.mahendhar.urlshortener.config.AppProperties;
import com.mahendhar.urlshortener.dto.CreateShortUrlRequest;
import com.mahendhar.urlshortener.dto.ShortUrlResponse;
import com.mahendhar.urlshortener.entity.ShortUrl;
import com.mahendhar.urlshortener.entity.User;
import com.mahendhar.urlshortener.exception.NotFoundException;
import com.mahendhar.urlshortener.repository.ShortUrlRepository;
import com.mahendhar.urlshortener.repository.UserRepository;
import com.mahendhar.urlshortener.util.ShortCodeGenerator;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UrlShortenerService {

    private static final Logger log = LoggerFactory.getLogger(UrlShortenerService.class);
    private static final int MAX_CODE_GENERATION_ATTEMPTS = 8;

    private final ShortUrlRepository shortUrlRepository;
    private final UserRepository userRepository;
    private final ShortCodeGenerator shortCodeGenerator;
    private final AppProperties appProperties;

    @Transactional
    public ShortUrlResponse create(CreateShortUrlRequest request, String userEmail) {
        log.info("Creating short URL for user {} originalUrl={}", userEmail, request.originalUrl());
        User owner = userRepository.findByEmail(userEmail.toLowerCase())
                .orElseThrow(() -> new NotFoundException("User not found"));
        ShortUrl shortUrl = ShortUrl.builder()
                .originalUrl(request.originalUrl())
                .shortCode(generateUniqueCode())
                .clickCount(0)
                .active(true)
                .expiresAt(request.expiresAt())
                .owner(owner)
                .build();
        ShortUrl saved = shortUrlRepository.save(shortUrl);
        log.debug("Saved short URL id={} code={}", saved.getId(), saved.getShortCode());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ShortUrlResponse> listForUser(String userEmail) {
        log.debug("Listing short URLs for user {}", userEmail);
        return shortUrlRepository.findAllByOwnerEmailOrderByCreatedAtDesc(userEmail.toLowerCase())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ShortUrlResponse getForUser(String shortCode, String userEmail) {
        log.debug("Getting short URL {} for user {}", shortCode, userEmail);
        return toResponse(findOwned(shortCode, userEmail));
    }

    @Transactional
    public void deactivate(String shortCode, String userEmail) {
        log.info("Deactivating short code {} for user {}", shortCode, userEmail);
        ShortUrl shortUrl = findOwned(shortCode, userEmail);
        shortUrl.setActive(false);
        shortUrlRepository.save(shortUrl);
    }

    @Transactional
    public String resolveRedirect(String shortCode) {
        log.info("Resolving redirect for code {}", shortCode);
        ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new NotFoundException("Short URL not found"));
        if (!shortUrl.isActive() || shortUrl.isExpired(Instant.now())) {
            log.warn("Short URL {} is inactive or expired", shortCode);
            throw new NotFoundException("Short URL not found");
        }
        shortUrl.setClickCount(shortUrl.getClickCount() + 1);
        shortUrlRepository.save(shortUrl);
        log.debug("Incremented click count for {} to {}", shortCode, shortUrl.getClickCount());
        return shortUrl.getOriginalUrl();
    }

    private ShortUrl findOwned(String shortCode, String userEmail) {
        return shortUrlRepository.findByShortCodeAndOwnerEmail(shortCode, userEmail.toLowerCase())
                .orElseThrow(() -> new NotFoundException("Short URL not found"));
    }

    private String generateUniqueCode() {
        for (int i = 0; i < MAX_CODE_GENERATION_ATTEMPTS; i++) {
            String code = shortCodeGenerator.generate(appProperties.shortCodeLength());
            if (!shortUrlRepository.existsByShortCode(code)) {
                if (i > 0) log.warn("Generated unique code after {} attempts: {}", i + 1, code);
                return code;
            }
            log.debug("Collision detected for generated code {} (attempt {})", code, i + 1);
        }
        log.error("Unable to generate a unique short code after {} attempts", MAX_CODE_GENERATION_ATTEMPTS);
        throw new IllegalStateException("Unable to generate a unique short code");
    }

    private ShortUrlResponse toResponse(ShortUrl shortUrl) {
        String baseUrl = appProperties.baseUrl().replaceAll("/+$", "");
        return new ShortUrlResponse(
                shortUrl.getId(),
                shortUrl.getOriginalUrl(),
                shortUrl.getShortCode(),
                baseUrl + "/" + shortUrl.getShortCode(),
                shortUrl.getClickCount(),
                shortUrl.isActive(),
                shortUrl.getExpiresAt(),
                shortUrl.getCreatedAt(),
                shortUrl.getUpdatedAt()
        );
    }
}

