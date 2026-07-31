package com.mahendhar.urlshortener.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.mahendhar.urlshortener.entity.User;
import com.mahendhar.urlshortener.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserDetailsServiceImpl service;

    @Test
    void loadUserNormalizesEmailAndAssignsUserRole() {
        User user = User.builder().email("member@example.com").passwordHash("hash").enabled(true).build();
        when(userRepository.findByEmail("member@example.com")).thenReturn(Optional.of(user));

        var result = service.loadUserByUsername("MEMBER@EXAMPLE.COM");

        assertThat(result.getUsername()).isEqualTo("member@example.com");
        assertThat(result.getAuthorities()).extracting("authority").contains("ROLE_USER");
    }

    @Test
    void missingUserRaisesSecuritySpecificException() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("missing@example.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
