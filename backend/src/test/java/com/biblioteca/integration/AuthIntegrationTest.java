package com.biblioteca.integration;

import com.biblioteca.dto.request.LoginRequest;
import com.biblioteca.dto.request.RegisterRequest;
import com.biblioteca.dto.response.AuthResponse;
import com.biblioteca.dto.response.UserResponse;
import com.biblioteca.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Auth Integration Tests")
class AuthIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("RF-001: Deve registrar novo usuário com sucesso")
    void shouldRegisterUserSuccessfully() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("joao");
        request.setEmail("joao@test.com");
        request.setPassword("senha123");

        ResponseEntity<UserResponse> response = restTemplate.postForEntity(
            "/api/auth/register", request, UserResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUsername()).isEqualTo("joao");
        assertThat(response.getBody().getEmail()).isEqualTo("joao@test.com");
        assertThat(response.getBody().getId()).isNotNull();
    }

    @Test
    @DisplayName("RF-001: Deve rejeitar registro com username duplicado")
    void shouldRejectDuplicateUsername() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("joao");
        request.setEmail("joao@test.com");
        request.setPassword("senha123");
        restTemplate.postForEntity("/api/auth/register", request, UserResponse.class);

        RegisterRequest duplicate = new RegisterRequest();
        duplicate.setUsername("joao");
        duplicate.setEmail("outro@test.com");
        duplicate.setPassword("senha123");

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/auth/register", duplicate, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("RF-002: Deve realizar login e retornar JWT")
    void shouldLoginAndReturnJwt() {
        RegisterRequest reg = new RegisterRequest();
        reg.setUsername("maria");
        reg.setEmail("maria@test.com");
        reg.setPassword("senha123");
        restTemplate.postForEntity("/api/auth/register", reg, UserResponse.class);

        LoginRequest login = new LoginRequest();
        login.setUsername("maria");
        login.setPassword("senha123");

        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
            "/api/auth/login", login, AuthResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getToken()).isNotBlank();
        assertThat(response.getBody().getType()).isEqualTo("Bearer");
    }

    @Test
    @DisplayName("RF-002: Deve rejeitar login com credenciais inválidas")
    void shouldRejectInvalidCredentials() {
        LoginRequest login = new LoginRequest();
        login.setUsername("inexistente");
        login.setPassword("errada");

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/auth/login", login, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
