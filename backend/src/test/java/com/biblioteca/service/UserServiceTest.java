package com.biblioteca.service;

import com.biblioteca.dto.request.RegisterRequest;
import com.biblioteca.dto.response.UserResponse;
import com.biblioteca.exception.DuplicateResourceException;
import com.biblioteca.exception.ResourceNotFoundException;
import com.biblioteca.model.User;
import com.biblioteca.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService - Testes Unitários (Caixa Branca)")
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private UserService userService;

    @Test
    @DisplayName("register: deve registrar usuário com senha criptografada")
    void shouldRegisterUserWithEncodedPassword() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("joao"); req.setEmail("joao@test.com"); req.setPassword("raw");

        when(userRepository.existsByUsername("joao")).thenReturn(false);
        when(userRepository.existsByEmail("joao@test.com")).thenReturn(false);
        when(passwordEncoder.encode("raw")).thenReturn("hashed");
        User saved = User.builder().id("1").username("joao").email("joao@test.com")
            .password("hashed").active(true).createdAt(LocalDateTime.now()).build();
        when(userRepository.save(any())).thenReturn(saved);

        UserResponse result = userService.register(req);

        assertThat(result.getUsername()).isEqualTo("joao");
        verify(passwordEncoder).encode("raw");
        verify(userRepository).save(argThat(u -> u.getPassword().equals("hashed")));
    }

    @Test
    @DisplayName("register: deve lançar erro para username duplicado")
    void shouldThrowForDuplicateUsername() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("joao"); req.setEmail("joao@test.com"); req.setPassword("senha");
        when(userRepository.existsByUsername("joao")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(req))
            .isInstanceOf(DuplicateResourceException.class)
            .hasMessageContaining("joao");
    }

    @Test
    @DisplayName("findById: deve lançar erro para ID inexistente")
    void shouldThrowForInvalidId() {
        when(userRepository.findById("invalid")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.findById("invalid"))
            .isInstanceOf(ResourceNotFoundException.class);
    }
}
