package com.mahendhar.urlshortener.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mahendhar.urlshortener.dto.AuthResponse;
import com.mahendhar.urlshortener.dto.LoginRequest;
import com.mahendhar.urlshortener.dto.RegisterRequest;
import com.mahendhar.urlshortener.service.AuthService;
import org.junit.jupiter.api.Test;

class AuthControllerTest {

    private final AuthService authService = mock(AuthService.class);
    private final AuthController controller = new AuthController(authService);

    @Test
    void registrationReturnsCreatedResponse() {
        RegisterRequest request = new RegisterRequest("Asha", "asha@example.com", "password123");
        AuthResponse response = mock(AuthResponse.class);
        when(authService.register(request)).thenReturn(response);

        var result = controller.register(request);

        assertThat(result.getStatusCode().value()).isEqualTo(201);
        assertThat(result.getBody()).isSameAs(response);
    }

    @Test
    void loginDelegatesToAuthenticationService() {
        LoginRequest request = new LoginRequest("asha@example.com", "password123");
        AuthResponse response = mock(AuthResponse.class);
        when(authService.login(request)).thenReturn(response);

        assertThat(controller.login(request).getBody()).isSameAs(response);
        verify(authService).login(request);
    }
}
