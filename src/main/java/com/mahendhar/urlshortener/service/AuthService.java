package com.mahendhar.urlshortener.service;

import com.mahendhar.urlshortener.dto.AuthResponse;
import com.mahendhar.urlshortener.dto.LoginRequest;
import com.mahendhar.urlshortener.dto.RegisterRequest;
import com.mahendhar.urlshortener.dto.UserResponse;
import com.mahendhar.urlshortener.entity.User;
import com.mahendhar.urlshortener.exception.ConflictException;
import com.mahendhar.urlshortener.repository.UserRepository;
import com.mahendhar.urlshortener.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().toLowerCase();
        log.info("Registering user {}", email);
        if (userRepository.existsByEmail(email)) {
            log.warn("Registration rejected because email already exists: {}", email);
            throw new ConflictException("Email is already registered");
        }

        User user = User.builder()
                .name(request.name().trim())
                .email(email)
                .passwordHash(passwordEncoder.encode(request.password()))
                .enabled(true)
                .build();
        User saved = userRepository.save(user);
        UserDetails userDetails = userDetailsService.loadUserByUsername(saved.getEmail());
        return tokenResponse(saved, userDetails);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.email().toLowerCase();
        log.info("Authenticating user {}", email);
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));
        User user = userRepository.findByEmail(email).orElseThrow();
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        return tokenResponse(user, userDetails);
    }

    private AuthResponse tokenResponse(User user, UserDetails userDetails) {
        return new AuthResponse(
                jwtService.generateToken(userDetails),
                "Bearer",
                jwtService.expiresAt(),
                new UserResponse(user.getId(), user.getName(), user.getEmail())
        );
    }
}

