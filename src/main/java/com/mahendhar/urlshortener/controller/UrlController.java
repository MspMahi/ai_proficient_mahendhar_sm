package com.mahendhar.urlshortener.controller;

import com.mahendhar.urlshortener.dto.CreateShortUrlRequest;
import com.mahendhar.urlshortener.dto.ShortUrlResponse;
import com.mahendhar.urlshortener.exception.UnauthorizedException;
import com.mahendhar.urlshortener.service.UrlShortenerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/urls")
@RequiredArgsConstructor
@Tag(name = "Short URLs")
@SecurityRequirement(name = "bearerAuth")
@ApiResponse(responseCode = "401", description = "A valid JWT bearer token is required")
public class UrlController {

    private static final Logger log = LoggerFactory.getLogger(UrlController.class);

    private final UrlShortenerService urlShortenerService;

    @PostMapping
    @Operation(summary = "Create a short URL")
    public ResponseEntity<ShortUrlResponse> create(@Valid @RequestBody CreateShortUrlRequest request, Principal principal) {
            String email = authenticatedEmail(principal);
            log.debug("POST /api/v1/urls create called by {}", email);
            return ResponseEntity.status(HttpStatus.CREATED).body(urlShortenerService.create(request, email));
        }

    @GetMapping
    @Operation(summary = "List the current user's short URLs")
    public ResponseEntity<List<ShortUrlResponse>> list(Principal principal) {
            String email = authenticatedEmail(principal);
            log.debug("GET /api/v1/urls list called by {}", email);
            return ResponseEntity.ok(urlShortenerService.listForUser(email));
        }

    @GetMapping("/{code}")
    @Operation(summary = "Get short URL details")
    public ResponseEntity<ShortUrlResponse> get(@PathVariable String code, Principal principal) {
            String email = authenticatedEmail(principal);
            log.debug("GET /api/v1/urls/{} called by {}", code, email);
            return ResponseEntity.ok(urlShortenerService.getForUser(code, email));
        }

    @DeleteMapping("/{code}")
    @Operation(summary = "Deactivate a short URL")
    public ResponseEntity<Void> deactivate(@PathVariable String code, Principal principal) {
            String email = authenticatedEmail(principal);
            log.debug("DELETE /api/v1/urls/{} called by {}", code, email);
            urlShortenerService.deactivate(code, email);
            return ResponseEntity.noContent().build();
        }

    private String authenticatedEmail(Principal principal) {
        if (principal == null) {
            throw new UnauthorizedException("Authentication is required");
        }
        return principal.getName();
    }
}

