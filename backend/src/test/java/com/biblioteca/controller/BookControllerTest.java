package com.biblioteca.controller;

import com.biblioteca.dto.request.BookRequest;
import com.biblioteca.dto.response.BookResponse;
import com.biblioteca.model.Book.ReadingStatus;
import com.biblioteca.model.User;
import com.biblioteca.repository.UserRepository;
import com.biblioteca.security.JwtService;
import com.biblioteca.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
@DisplayName("BookController - Testes Caixa Preta (E2E Controller)")
class BookControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private BookService bookService;
    @MockBean private UserRepository userRepository;
    @MockBean private JwtService jwtService;
    @MockBean private UserDetailsService userDetailsService;

    private BookResponse buildResponse(String id, String title) {
        return BookResponse.builder()
                .id(id).title(title).author("Autor")
                .status(ReadingStatus.TO_READ).build();
    }

    private User buildUser() {
        return User.builder().id("uid").username("user")
                .email("user@test.com").password("pass").build();
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/books - deve retornar 200 com lista de livros")
    void shouldReturn200WithBooks() throws Exception {
        when(userRepository.findByUsername(any())).thenReturn(Optional.of(buildUser()));
        when(bookService.findAllByUser("uid")).thenReturn(List.of(
                buildResponse("1", "Livro A"), buildResponse("2", "Livro B")));

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Livro A"));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/books - deve retornar 201 ao criar livro válido")
    void shouldReturn201OnCreate() throws Exception {
        BookRequest request = new BookRequest();
        request.setTitle("Novo Livro");
        request.setAuthor("Autor");
        BookResponse response = buildResponse("1", "Novo Livro");

        when(userRepository.findByUsername(any())).thenReturn(Optional.of(buildUser()));
        when(bookService.create(any(), eq("uid"))).thenReturn(response);

        mockMvc.perform(post("/api/books")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("1"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @WithMockUser
    @DisplayName("POST /api/books - deve retornar 400 quando título está vazio ou nulo")
    void shouldReturn400WhenTitleIsBlank(String title) throws Exception {
        BookRequest request = new BookRequest();
        request.setTitle(title);
        request.setAuthor("Autor");

        mockMvc.perform(post("/api/books")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/books - deve retornar 401 sem autenticação")
    void shouldReturn401WithoutAuth() throws Exception {
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isUnauthorized());
    }
}