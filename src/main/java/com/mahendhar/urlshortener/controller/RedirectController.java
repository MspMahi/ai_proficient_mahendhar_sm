package com.mahendhar.urlshortener.controller;

import com.mahendhar.urlshortener.service.UrlShortenerService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RedirectController {

    private static final Logger log = LoggerFactory.getLogger(RedirectController.class);

    private final UrlShortenerService urlShortenerService;

    @GetMapping("/{code:[A-Za-z0-9]{6,32}}")
    @Operation(summary = "Redirect to the original URL")
    public ResponseEntity<Void> redirect(@PathVariable String code, HttpServletResponse response) {
        log.info("Redirect requested for code={}", code);
        String targetUrl = urlShortenerService.resolveRedirect(code);
        log.debug("Redirecting code={}", code);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, targetUrl)
                .build();
    }
}

