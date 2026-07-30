package com.mahendhar.urlshortener.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        @NotBlank String baseUrl,
        @Min(6) int shortCodeLength,
        @Valid @NotNull Jwt jwt
) {

    public record Jwt(
            @NotBlank String secret,
            @NotNull Duration expiration
    ) {
    }
}

