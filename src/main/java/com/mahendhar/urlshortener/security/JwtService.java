package com.mahendhar.urlshortener.security;

import com.mahendhar.urlshortener.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final AppProperties appProperties;

    public String generateToken(UserDetails userDetails) {
            log.debug("Generating token for {}", userDetails.getUsername());
            Instant now = Instant.now();
            Instant expiresAt = now.plus(appProperties.jwt().expiration());
            return Jwts.builder()
                    .subject(userDetails.getUsername())
                    .issuedAt(Date.from(now))
                    .expiration(Date.from(expiresAt))
                    .signWith(signingKey())
                    .compact();
        }

    public Instant expiresAt() {
        return Instant.now().plus(appProperties.jwt().expiration());
    }

    public String extractSubject(String token) {
        try {
            return claims(token).getSubject();
        } catch (RuntimeException ex) {
                log.debug("Failed to extract subject from token: {}", ex.getMessage());
                return null;
            }
        }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String subject = extractSubject(token);
        return subject != null && subject.equals(userDetails.getUsername()) && !isExpired(token);
    }

    private boolean isExpired(String token) {
        try {
            return claims(token).getExpiration().before(new Date());
        } catch (RuntimeException ex) {
            return true;
        }
    }

    private Claims claims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(java.util.Base64.getDecoder().decode(appProperties.jwt().secret()));
    }
}

