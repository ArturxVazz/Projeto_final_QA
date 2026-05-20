package com.library.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.dto.AuthDTOs;
import com.library.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("AuthController - Testes de Integração (Caixa Preta)")
class AuthControllerIT extends BaseIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("RF01 - Deve registrar novo usuário com sucesso e retornar JWT")
    void shouldRegisterNewUserSuccessfully() throws Exception {
        AuthDTOs.RegisterRequest request = new AuthDTOs.RegisterRequest();
        request.setUsername("joao_silva");
        request.setEmail("joao@email.com");
        request.setPassword("senha123");

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value("joao_silva"))
                .andExpect(jsonPath("$.userId").isNotEmpty())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThat(body).contains("token");

        assertThat(userRepository.existsByUsername("joao_silva")).isTrue();
    }

    @Test
    @DisplayName("RF01 - Deve retornar 409 ao registrar usuário com username duplicado")
    void shouldReturn409WhenUsernameAlreadyExists() throws Exception {
        AuthDTOs.RegisterRequest request = new AuthDTOs.RegisterRequest();
        request.setUsername("usuario_existente");
        request.setEmail("primeiro@email.com");
        request.setPassword("senha123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        AuthDTOs.RegisterRequest duplicate = new AuthDTOs.RegisterRequest();
        duplicate.setUsername("usuario_existente");
        duplicate.setEmail("segundo@email.com");
        duplicate.setPassword("outrasenha");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Username já está em uso"));
    }

    @Test
    @DisplayName("RF01 - Deve retornar 409 ao registrar com email duplicado")
    void shouldReturn409WhenEmailAlreadyExists() throws Exception {
        AuthDTOs.RegisterRequest first = new AuthDTOs.RegisterRequest();
        first.setUsername("user1");
        first.setEmail("igual@email.com");
        first.setPassword("senha123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(first)))
                .andExpect(status().isCreated());

        AuthDTOs.RegisterRequest second = new AuthDTOs.RegisterRequest();
        second.setUsername("user2");
        second.setEmail("igual@email.com");
        second.setPassword("senha456");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(second)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email já está em uso"));
    }

    @Test
    @DisplayName("RF01 - Deve retornar 400 ao registrar com campos inválidos")
    void shouldReturn400WhenRegistrationDataIsInvalid() throws Exception {
        AuthDTOs.RegisterRequest request = new AuthDTOs.RegisterRequest();
        request.setUsername("ab"); // muito curto
        request.setEmail("email-invalido");
        request.setPassword("123"); // muito curta

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("RF02 - Deve autenticar usuário com credenciais válidas")
    void shouldAuthenticateUserWithValidCredentials() throws Exception {
        // Registrar usuário
        AuthDTOs.RegisterRequest reg = new AuthDTOs.RegisterRequest();
        reg.setUsername("maria_login");
        reg.setEmail("maria@email.com");
        reg.setPassword("senha_segura");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated());

        // Login
        AuthDTOs.LoginRequest login = new AuthDTOs.LoginRequest();
        login.setUsername("maria_login");
        login.setPassword("senha_segura");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value("maria_login"));
    }

    @Test
    @DisplayName("RF02 - Deve retornar 401 ao autenticar com senha incorreta")
    void shouldReturn401WhenPasswordIsWrong() throws Exception {
        AuthDTOs.RegisterRequest reg = new AuthDTOs.RegisterRequest();
        reg.setUsername("teste_login");
        reg.setEmail("teste@email.com");
        reg.setPassword("senha_correta");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated());

        AuthDTOs.LoginRequest login = new AuthDTOs.LoginRequest();
        login.setUsername("teste_login");
        login.setPassword("senha_errada");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Credenciais inválidas"));
    }

    @Test
    @DisplayName("RF02 - Deve retornar 401 ao autenticar usuário inexistente")
    void shouldReturn401WhenUserDoesNotExist() throws Exception {
        AuthDTOs.LoginRequest login = new AuthDTOs.LoginRequest();
        login.setUsername("nao_existe");
        login.setPassword("qualquer_senha");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());
    }
}
