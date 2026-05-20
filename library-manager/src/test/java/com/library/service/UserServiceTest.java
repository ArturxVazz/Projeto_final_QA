package com.library.service;

import com.library.dto.AuthDTOs;
import com.library.model.User;
import com.library.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@DisplayName("UserService - Testes de Integração com Testcontainers")
class UserServiceTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired UserService userService;
    @Autowired UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve registrar novo usuário e retornar token JWT válido")
    void shouldRegisterNewUserAndReturnValidToken() {
        AuthDTOs.RegisterRequest request = new AuthDTOs.RegisterRequest();
        request.setUsername("ana_teste");
        request.setEmail("ana@email.com");
        request.setPassword("senha_segura");

        AuthDTOs.AuthResponse response = userService.register(request);

        assertThat(response.getToken()).isNotBlank();
        assertThat(response.getUsername()).isEqualTo("ana_teste");
        assertThat(response.getUserId()).isNotNull();

        // Verificar persistência no MongoDB via Testcontainer
        assertThat(userRepository.existsByUsername("ana_teste")).isTrue();
        User persisted = userRepository.findByUsername("ana_teste").orElseThrow();
        assertThat(persisted.getPassword()).isNotEqualTo("senha_segura"); // deve estar hasheado
    }

    @Test
    @DisplayName("Deve hashear a senha antes de persistir")
    void shouldHashPasswordBeforeSaving() {
        AuthDTOs.RegisterRequest request = new AuthDTOs.RegisterRequest();
        request.setUsername("carlos");
        request.setEmail("carlos@email.com");
        request.setPassword("minha_senha_123");

        userService.register(request);

        User user = userRepository.findByUsername("carlos").orElseThrow();
        assertThat(user.getPassword()).isNotEqualTo("minha_senha_123");
        assertThat(user.getPassword()).startsWith("$2a$"); // BCrypt hash
    }

    @Test
    @DisplayName("Deve lançar exceção ao registrar username duplicado")
    void shouldThrowExceptionWhenUsernameIsDuplicated() {
        AuthDTOs.RegisterRequest first = new AuthDTOs.RegisterRequest();
        first.setUsername("duplicado");
        first.setEmail("primeiro@email.com");
        first.setPassword("senha1");

        userService.register(first);

        AuthDTOs.RegisterRequest second = new AuthDTOs.RegisterRequest();
        second.setUsername("duplicado");
        second.setEmail("segundo@email.com");
        second.setPassword("senha2");

        assertThatThrownBy(() -> userService.register(second))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username já está em uso");
    }

    @Test
    @DisplayName("Deve lançar exceção ao registrar email duplicado")
    void shouldThrowExceptionWhenEmailIsDuplicated() {
        AuthDTOs.RegisterRequest first = new AuthDTOs.RegisterRequest();
        first.setUsername("user_a");
        first.setEmail("mesmo@email.com");
        first.setPassword("senha1");

        userService.register(first);

        AuthDTOs.RegisterRequest second = new AuthDTOs.RegisterRequest();
        second.setUsername("user_b");
        second.setEmail("mesmo@email.com");
        second.setPassword("senha2");

        assertThatThrownBy(() -> userService.register(second))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email já está em uso");
    }

    @Test
    @DisplayName("Deve autenticar usuário com credenciais válidas e retornar JWT")
    void shouldLoginWithValidCredentials() {
        AuthDTOs.RegisterRequest reg = new AuthDTOs.RegisterRequest();
        reg.setUsername("fernanda");
        reg.setEmail("fernanda@email.com");
        reg.setPassword("senha_fer");

        userService.register(reg);

        AuthDTOs.LoginRequest login = new AuthDTOs.LoginRequest();
        login.setUsername("fernanda");
        login.setPassword("senha_fer");

        AuthDTOs.AuthResponse response = userService.login(login);

        assertThat(response.getToken()).isNotBlank();
        assertThat(response.getUsername()).isEqualTo("fernanda");
    }

    @Test
    @DisplayName("Deve lançar exceção ao login com senha incorreta")
    void shouldThrowExceptionWhenPasswordIsWrong() {
        AuthDTOs.RegisterRequest reg = new AuthDTOs.RegisterRequest();
        reg.setUsername("gabriel");
        reg.setEmail("gabriel@email.com");
        reg.setPassword("senha_correta");

        userService.register(reg);

        AuthDTOs.LoginRequest login = new AuthDTOs.LoginRequest();
        login.setUsername("gabriel");
        login.setPassword("senha_errada");

        assertThatThrownBy(() -> userService.login(login))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Credenciais inválidas");
    }

    @Test
    @DisplayName("Deve lançar exceção ao login com usuário inexistente")
    void shouldThrowExceptionWhenUserDoesNotExist() {
        AuthDTOs.LoginRequest login = new AuthDTOs.LoginRequest();
        login.setUsername("nao_existe");
        login.setPassword("qualquer");

        assertThatThrownBy(() -> userService.login(login))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Credenciais inválidas");
    }

    @Test
    @DisplayName("Deve persistir data de criação automaticamente")
    void shouldPersistCreationDateAutomatically() {
        AuthDTOs.RegisterRequest reg = new AuthDTOs.RegisterRequest();
        reg.setUsername("helena");
        reg.setEmail("helena@email.com");
        reg.setPassword("senha_h");

        userService.register(reg);

        User user = userRepository.findByUsername("helena").orElseThrow();
        assertThat(user.getCreatedAt()).isNotNull();
    }
}
