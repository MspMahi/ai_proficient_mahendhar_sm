package com.mahendhar.urlshortener.service;

import com.mahendhar.urlshortener.config.AppProperties;
import com.mahendhar.urlshortener.dto.CreateShortUrlRequest;
import com.mahendhar.urlshortener.entity.ShortUrl;
import com.mahendhar.urlshortener.entity.User;
import com.mahendhar.urlshortener.exception.NotFoundException;
import com.mahendhar.urlshortener.repository.ShortUrlRepository;
import com.mahendhar.urlshortener.repository.UserRepository;
import com.mahendhar.urlshortener.util.ShortCodeGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlShortenerServiceTest {

    @Mock
    ShortUrlRepository shortUrlRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    ShortCodeGenerator shortCodeGenerator;

    AppProperties appProperties;

    @InjectMocks
    UrlShortenerService urlShortenerService;

    @BeforeEach
    void setUp() {
        appProperties = new AppProperties("http://localhost:8080", 6, new AppProperties.Jwt("secret", Duration.ofHours(1)));
        // inject appProperties via constructor
        urlShortenerService = new UrlShortenerService(shortUrlRepository, userRepository, shortCodeGenerator, appProperties);
    }

    @Test
    void create_shouldSaveAndReturnResponse() {
        User user = User.builder().id(10L).email("test@example.com").name("Test").passwordHash("x").enabled(true).build();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(shortCodeGenerator.generate(6)).thenReturn("ABC123");
        when(shortUrlRepository.existsByShortCode("ABC123")).thenReturn(false);
        when(shortUrlRepository.save(any())).thenAnswer(invocation -> {
            ShortUrl s = invocation.getArgument(0);
            s.setId(1L);
            return s;
        });

        CreateShortUrlRequest req = new CreateShortUrlRequest("https://example.com", null);
        var resp = urlShortenerService.create(req, "test@example.com");

        assertThat(resp.id()).isEqualTo(1L);
        assertThat(resp.shortCode()).isEqualTo("ABC123");
        assertThat(resp.shortUrl()).isEqualTo("http://localhost:8080/ABC123");

        ArgumentCaptor<ShortUrl> captor = ArgumentCaptor.forClass(ShortUrl.class);
        verify(shortUrlRepository).save(captor.capture());
        assertThat(captor.getValue().getOwner().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void resolveRedirect_shouldReturnOriginalUrlAndIncrementClickCount() {
        ShortUrl s = ShortUrl.builder().id(5L).originalUrl("https://a.com").shortCode("X1").clickCount(2).active(true).owner(User.builder().id(1L).email("u@test.com").build()).build();
        when(shortUrlRepository.findByShortCode("X1")).thenReturn(Optional.of(s));
        when(shortUrlRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        String resolved = urlShortenerService.resolveRedirect("X1");

        assertThat(resolved).isEqualTo("https://a.com");
        assertThat(s.getClickCount()).isEqualTo(3);
        verify(shortUrlRepository).save(s);
    }

    @Test
    void resolveRedirect_inactiveOrExpiredShouldThrow() {
        ShortUrl s = ShortUrl.builder().id(6L).originalUrl("https://b.com").shortCode("Y2").clickCount(0).active(false).owner(User.builder().id(1L).email("u@test.com").build()).build();
        when(shortUrlRepository.findByShortCode("Y2")).thenReturn(Optional.of(s));

        assertThatThrownBy(() -> urlShortenerService.resolveRedirect("Y2")).isInstanceOf(NotFoundException.class);
    }

    @Test
    void generateUniqueCode_retriesOnCollision() {
        when(shortCodeGenerator.generate(6)).thenReturn("AAA","BBB");
        when(shortUrlRepository.existsByShortCode("AAA")).thenReturn(true);
        when(shortUrlRepository.existsByShortCode("BBB")).thenReturn(false);

        // invoke via create path: need a user
        User user = User.builder().id(2L).email("u2@test.com").name("U2").passwordHash("p").enabled(true).build();
        when(userRepository.findByEmail("u2@test.com")).thenReturn(Optional.of(user));
        when(shortUrlRepository.save(any())).thenAnswer(invocation -> {
            ShortUrl s = invocation.getArgument(0);
            s.setId(99L);
            return s;
        });

        CreateShortUrlRequest req = new CreateShortUrlRequest("https://retry.com", null);
        var resp = urlShortenerService.create(req, "u2@test.com");

        assertThat(resp.shortCode()).isEqualTo("BBB");
        verify(shortCodeGenerator, times(2)).generate(6);
    }
}
