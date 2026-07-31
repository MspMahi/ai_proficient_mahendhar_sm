package com.mahendhar.urlshortener.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.mahendhar.urlshortener.config.AppProperties;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService(new AppProperties(
            "http://localhost:8080", 8,
            new AppProperties.Jwt("VGhpcy1pcy1hLXRlc3Qtb25seS1qd3Qtc2VjcmV0LWtleS1vdmVyLTMyLWJ5dGVz", Duration.ofMinutes(10))));

    @Test
    void tokenRoundTripExtractsSubjectAndValidatesOwner() {
        UserDetails owner = User.withUsername("owner@example.com").password("unused").roles("USER").build();
        String token = jwtService.generateToken(owner);

        assertThat(jwtService.extractSubject(token)).isEqualTo("owner@example.com");
        assertThat(jwtService.isTokenValid(token, owner)).isTrue();
    }

    @Test
    void invalidTokenIsRejectedWithoutThrowing() {
        UserDetails user = User.withUsername("owner@example.com").password("unused").roles("USER").build();

        assertThat(jwtService.extractSubject("not-a-token")).isNull();
        assertThat(jwtService.isTokenValid("not-a-token", user)).isFalse();
    }
}
