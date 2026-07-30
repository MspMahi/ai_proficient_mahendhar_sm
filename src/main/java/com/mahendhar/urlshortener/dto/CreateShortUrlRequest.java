package com.mahendhar.urlshortener.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;
import java.time.Instant;

public record CreateShortUrlRequest(
        @Schema(
                description = "The complete destination URL, including http:// or https://",
                example = "https://example.com/products/widget"
        )
        @NotBlank @URL String originalUrl,
        @Schema(
                description = "Optional UTC expiration timestamp. If supplied, it must be in the future.",
                example = "2099-12-31T23:59:59Z",
                nullable = true
        )
        @Future Instant expiresAt
) {
}

