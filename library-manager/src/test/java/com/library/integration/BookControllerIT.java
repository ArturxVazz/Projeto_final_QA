package com.library.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.dto.AuthDTOs;
import com.library.dto.BookDTOs;
import com.library.model.Book;
import com.library.repository.BookRepository;
import com.library.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("BookController - Testes de Integração (Caixa Preta)")
class BookControllerIT extends BaseIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired BookRepository bookRepository;
    @Autowired UserRepository userRepository;

    private String jwtToken;
    private String userId;

    @BeforeEach
    void setUp() throws Exception {
        bookRepository.deleteAll();
        userRepository.deleteAll();

        AuthDTOs.RegisterRequest reg = new AuthDTOs.RegisterRequest();
        reg.setUsername("test_user");
        reg.setEmail("test@email.com");
        reg.setPassword("senha123");

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)))
                .andReturn();

        AuthDTOs.AuthResponse auth = objectMapper.readValue(
                result.getResponse().getContentAsString(), AuthDTOs.AuthResponse.class);
        jwtToken = auth.getToken();
        userId = auth.getUserId();
    }

    @Test
    @DisplayName("RF03 - Deve criar um novo livro com sucesso")
    void shouldCreateBookSuccessfully() throws Exception {
        BookDTOs.BookRequest request = new BookDTOs.BookRequest();
        request.setTitle("O Senhor dos Anéis");
        request.setAuthor("J.R.R. Tolkien");
        request.setGenre("Fantasia");
        request.setStatus(Book.ReadingStatus.WANT_TO_READ);

        MvcResult result = mockMvc.perform(post("/api/books")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.title").value("O Senhor dos Anéis"))
                .andExpect(jsonPath("$.author").value("J.R.R. Tolkien"))
                .andExpect(jsonPath("$.status").value("WANT_TO_READ"))
                .andReturn();

        assertThat(bookRepository.countByUserId(userId)).isEqualTo(1);
    }

    @Test
    @DisplayName("RF03 - Deve retornar 400 ao criar livro sem título")
    void shouldReturn400WhenTitleIsMissing() throws Exception {
        BookDTOs.BookRequest request = new BookDTOs.BookRequest();
        request.setAuthor("Algum Autor");

        mockMvc.perform(post("/api/books")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("RF03 - Deve retornar 401 ao criar livro sem autenticação")
    void shouldReturn401WhenNotAuthenticated() throws Exception {
        BookDTOs.BookRequest request = new BookDTOs.BookRequest();
        request.setTitle("Livro");
        request.setAuthor("Autor");

        mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("RF04 - Deve listar todos os livros do usuário")
    void shouldListAllBooksForUser() throws Exception {
        // Criar dois livros
        for (int i = 1; i <= 2; i++) {
            BookDTOs.BookRequest req = new BookDTOs.BookRequest();
            req.setTitle("Livro " + i);
            req.setAuthor("Autor " + i);
            mockMvc.perform(post("/api/books")
                    .header("Authorization", "Bearer " + jwtToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated());
        }

        mockMvc.perform(get("/api/books")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("RF04 - Deve buscar livros por título")
    void shouldSearchBooksByTitle() throws Exception {
        BookDTOs.BookRequest req1 = new BookDTOs.BookRequest();
        req1.setTitle("Harry Potter");
        req1.setAuthor("J.K. Rowling");

        BookDTOs.BookRequest req2 = new BookDTOs.BookRequest();
        req2.setTitle("Senhor dos Anéis");
        req2.setAuthor("Tolkien");

        mockMvc.perform(post("/api/books").header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req1))).andReturn();
        mockMvc.perform(post("/api/books").header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req2))).andReturn();

        mockMvc.perform(get("/api/books?search=Harry")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Harry Potter"));
    }

    @Test
    @DisplayName("RF05 - Deve atualizar um livro com sucesso")
    void shouldUpdateBookSuccessfully() throws Exception {
        BookDTOs.BookRequest create = new BookDTOs.BookRequest();
        create.setTitle("Título Original");
        create.setAuthor("Autor Original");

        MvcResult created = mockMvc.perform(post("/api/books")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(create)))
                .andExpect(status().isCreated())
                .andReturn();

        BookDTOs.BookResponse createdBook = objectMapper.readValue(
                created.getResponse().getContentAsString(), BookDTOs.BookResponse.class);

        BookDTOs.BookRequest update = new BookDTOs.BookRequest();
        update.setTitle("Título Atualizado");
        update.setAuthor("Autor Atualizado");
        update.setStatus(Book.ReadingStatus.READ);
        update.setRating(5);

        mockMvc.perform(put("/api/books/" + createdBook.getId())
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Título Atualizado"))
                .andExpect(jsonPath("$.status").value("READ"))
                .andExpect(jsonPath("$.rating").value(5));
    }

    @Test
    @DisplayName("RF05 - Deve retornar 404 ao atualizar livro de outro usuário")
    void shouldReturn404WhenUpdatingOtherUserBook() throws Exception {
        // Criar livro com user A (já registrado)
        BookDTOs.BookRequest req = new BookDTOs.BookRequest();
        req.setTitle("Livro do User A");
        req.setAuthor("Autor A");

        MvcResult created = mockMvc.perform(post("/api/books")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        BookDTOs.BookResponse book = objectMapper.readValue(
                created.getResponse().getContentAsString(), BookDTOs.BookResponse.class);

        // Registrar outro usuário
        AuthDTOs.RegisterRequest reg2 = new AuthDTOs.RegisterRequest();
        reg2.setUsername("outro_user");
        reg2.setEmail("outro@email.com");
        reg2.setPassword("senha456");

        MvcResult auth2 = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg2)))
                .andReturn();

        AuthDTOs.AuthResponse auth2Response = objectMapper.readValue(
                auth2.getResponse().getContentAsString(), AuthDTOs.AuthResponse.class);

        BookDTOs.BookRequest update = new BookDTOs.BookRequest();
        update.setTitle("Tentativa de atualização");
        update.setAuthor("Invasor");

        mockMvc.perform(put("/api/books/" + book.getId())
                .header("Authorization", "Bearer " + auth2Response.getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("RF06 - Deve excluir um livro com sucesso")
    void shouldDeleteBookSuccessfully() throws Exception {
        BookDTOs.BookRequest req = new BookDTOs.BookRequest();
        req.setTitle("Livro para Deletar");
        req.setAuthor("Autor");

        MvcResult created = mockMvc.perform(post("/api/books")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        BookDTOs.BookResponse book = objectMapper.readValue(
                created.getResponse().getContentAsString(), BookDTOs.BookResponse.class);

        mockMvc.perform(delete("/api/books/" + book.getId())
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());

        assertThat(bookRepository.countByUserId(userId)).isEqualTo(0);
    }

    @Test
    @DisplayName("RF07 - Deve retornar estatísticas corretas do usuário")
    void shouldReturnCorrectStats() throws Exception {
        // Criar livros com diferentes status
        createBook("Lido 1", Book.ReadingStatus.READ);
        createBook("Lido 2", Book.ReadingStatus.READ);
        createBook("Lendo", Book.ReadingStatus.READING);
        createBook("Para Ler", Book.ReadingStatus.WANT_TO_READ);

        mockMvc.perform(get("/api/books/stats")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(4))
                .andExpect(jsonPath("$.read").value(2))
                .andExpect(jsonPath("$.reading").value(1))
                .andExpect(jsonPath("$.wantToRead").value(1));
    }

    @Test
    @DisplayName("RF04 - Deve filtrar livros por status READING")
    void shouldFilterBooksByStatus() throws Exception {
        createBook("Livro 1", Book.ReadingStatus.READ);
        createBook("Livro 2", Book.ReadingStatus.READING);
        createBook("Livro 3", Book.ReadingStatus.READING);

        mockMvc.perform(get("/api/books?status=READING")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    private void createBook(String title, Book.ReadingStatus status) throws Exception {
        BookDTOs.BookRequest req = new BookDTOs.BookRequest();
        req.setTitle(title);
        req.setAuthor("Autor");
        req.setStatus(status);
        mockMvc.perform(post("/api/books")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));
    }
}
