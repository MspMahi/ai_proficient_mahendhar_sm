package com.mahendhar.urlshortener.repository;

import com.mahendhar.urlshortener.entity.ShortUrl;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {

    boolean existsByShortCode(String shortCode);

    Optional<ShortUrl> findByShortCode(String shortCode);

    Optional<ShortUrl> findByShortCodeAndOwnerEmail(String shortCode, String ownerEmail);

    List<ShortUrl> findAllByOwnerEmailOrderByCreatedAtDesc(String ownerEmail);
}

