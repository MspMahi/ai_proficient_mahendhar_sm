package com.mahendhar.urlshortener.controller;

import com.mahendhar.urlshortener.dto.ShortUrlResponse;
import com.mahendhar.urlshortener.security.JwtAuthenticationFilter;
import com.mahendhar.urlshortener.service.UrlShortenerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(
        controllers = UrlController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
)
@AutoConfigureMockMvc(addFilters = false)
class UrlControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    UrlShortenerService urlShortenerService;

    @Test
    void create_shouldReturnCreated() throws Exception {
        ShortUrlResponse resp = new ShortUrlResponse(1L, "https://example.com", "ABC123", "http://localhost:8080/ABC123", 0L, true, null, Instant.now(), Instant.now());

        Mockito.when(urlShortenerService.create(any(), eq("user@example.com"))).thenReturn(resp);

        Principal p = () -> "user@example.com";

        mockMvc.perform(post("/api/v1/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"originalUrl\":\"https://example.com\"}")
                        .principal(p))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortCode").value("ABC123"))
                .andExpect(jsonPath("$.shortUrl").value("http://localhost:8080/ABC123"));
    }

    @Test
    void get_shouldReturnOk() throws Exception {
        ShortUrlResponse resp = new ShortUrlResponse(2L, "https://x.com", "ZZZ999", "http://localhost:8080/ZZZ999", 1L, true, null, Instant.now(), Instant.now());
        Mockito.when(urlShortenerService.getForUser("ZZZ999", "user@example.com")).thenReturn(resp);
        Principal p = () -> "user@example.com";

        mockMvc.perform(get("/api/v1/urls/ZZZ999").principal(p))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.shortCode").value("ZZZ999"));
    }

    @Test
    void createWithInvalidUrlReturnsFieldLevelValidationDetails() throws Exception {
        Principal p = () -> "user@example.com";

        mockMvc.perform(post("/api/v1/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"originalUrl\":\"not-a-url\"}")
                        .principal(p))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.validationErrors.originalUrl").exists());
    }

    @Test
    void deleteDeactivatesUrlAndReturnsNoContent() throws Exception {
        Principal p = () -> "user@example.com";

        mockMvc.perform(delete("/api/v1/urls/ABC123").principal(p))
                .andExpect(status().isNoContent());

        Mockito.verify(urlShortenerService).deactivate("ABC123", "user@example.com");
    }
}
