package com.mahendhar.urlshortener.service;

import com.mahendhar.urlshortener.dto.LoginRequest;
import com.mahendhar.urlshortener.dto.RegisterRequest;
import com.mahendhar.urlshortener.entity.User;
import com.mahendhar.urlshortener.exception.ConflictException;
import com.mahendhar.urlshortener.repository.UserRepository;
import com.mahendhar.urlshortener.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    AuthenticationManager authenticationManager;

    @Mock
    UserDetailsService userDetailsService;

    @Mock
    JwtService jwtService;

    @InjectMocks
    AuthService authService;

    @BeforeEach
    void setUp() {
        // nothing
    }

    @Test
    void register_shouldSaveAndReturnToken() {
        RegisterRequest req = new RegisterRequest("Name","new@example.com","password123");
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");

        User saved = User.builder().id(7L).name("Name").email("new@example.com").passwordHash("hashed").enabled(true).build();
        when(userRepository.save(any())).thenReturn(saved);

        UserDetails ud = org.springframework.security.core.userdetails.User.withUsername("new@example.com").password("hashed").roles("USER").build();
        when(userDetailsService.loadUserByUsername("new@example.com")).thenReturn(ud);
        when(jwtService.generateToken(ud)).thenReturn("tok");
        when(jwtService.expiresAt()).thenReturn(Instant.now().plusSeconds(3600));

        var resp = authService.register(req);

        assertThat(resp.token()).isEqualTo("tok");
        assertThat(resp.user().email()).isEqualTo("new@example.com");
        verify(userRepository).save(any());
    }

    @Test
    void register_whenEmailExists_shouldThrowConflict() {
        RegisterRequest req = new RegisterRequest("Name","exists@example.com","password123");
        when(userRepository.existsByEmail("exists@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req)).isInstanceOf(ConflictException.class);
    }

    @Test
    void login_shouldAuthenticateAndReturnToken() {
        LoginRequest req = new LoginRequest("login@example.com","pwd");

        User user = User.builder().id(3L).name("L").email("login@example.com").passwordHash("h").enabled(true).build();
        when(userRepository.findByEmail("login@example.com")).thenReturn(Optional.of(user));

        UserDetails ud = org.springframework.security.core.userdetails.User.withUsername("login@example.com").password("h").roles("USER").build();
        when(userDetailsService.loadUserByUsername("login@example.com")).thenReturn(ud);
        when(jwtService.generateToken(ud)).thenReturn("tkn2");
        when(jwtService.expiresAt()).thenReturn(Instant.now().plusSeconds(3600));

        var resp = authService.login(req);

        assertThat(resp.token()).isEqualTo("tkn2");
        assertThat(resp.user().email()).isEqualTo("login@example.com");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }
}
