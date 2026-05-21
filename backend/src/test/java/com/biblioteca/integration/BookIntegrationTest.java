package com.biblioteca.integration;

import com.biblioteca.dto.request.BookRequest;
import com.biblioteca.dto.request.LoginRequest;
import com.biblioteca.dto.request.RegisterRequest;
import com.biblioteca.dto.response.AuthResponse;
import com.biblioteca.dto.response.BookResponse;
import com.biblioteca.dto.response.UserResponse;
import com.biblioteca.model.Book.ReadingStatus;
import com.biblioteca.repository.BookRepository;
import com.biblioteca.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Book Integration Tests")
class BookIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    private String token;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();
        userRepository.deleteAll();

        RegisterRequest reg = new RegisterRequest();
        reg.setUsername("testuser");
        reg.setEmail("test@test.com");
        reg.setPassword("senha123");
        restTemplate.postForEntity("/api/auth/register", reg, UserResponse.class);

        LoginRequest login = new LoginRequest();
        login.setUsername("testuser");
        login.setPassword("senha123");
        AuthResponse auth = restTemplate.postForEntity("/api/auth/login", login, AuthResponse.class).getBody();
        token = auth.getToken();
    }

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Test
    @DisplayName("RF-003: Deve criar livro com sucesso")
    void shouldCreateBook() {
        BookRequest request = new BookRequest();
        request.setTitle("Dom Casmurro");
        request.setAuthor("Machado de Assis");
        request.setIsbn("978-85-359-0277-5");
        request.setGenre("Romance");
        request.setYear(1899);
        request.setStatus(ReadingStatus.TO_READ);

        ResponseEntity<BookResponse> response = restTemplate.exchange(
            "/api/books", HttpMethod.POST,
            new HttpEntity<>(request, authHeaders()), BookResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("Dom Casmurro");
    }

    @Test
    @DisplayName("RF-004: Deve listar livros do usuário")
    void shouldListUserBooks() {
        BookRequest req1 = new BookRequest(); req1.setTitle("Livro 1"); req1.setAuthor("Autor 1");
        BookRequest req2 = new BookRequest(); req2.setTitle("Livro 2"); req2.setAuthor("Autor 2");
        restTemplate.exchange("/api/books", HttpMethod.POST, new HttpEntity<>(req1, authHeaders()), BookResponse.class);
        restTemplate.exchange("/api/books", HttpMethod.POST, new HttpEntity<>(req2, authHeaders()), BookResponse.class);

        ResponseEntity<List<BookResponse>> response = restTemplate.exchange(
            "/api/books", HttpMethod.GET,
            new HttpEntity<>(authHeaders()),
            new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
    }

    @Test
    @DisplayName("RF-005: Deve atualizar livro com sucesso")
    void shouldUpdateBook() {
        BookRequest create = new BookRequest(); create.setTitle("Original"); create.setAuthor("Autor");
        BookResponse created = restTemplate.exchange("/api/books", HttpMethod.POST,
            new HttpEntity<>(create, authHeaders()), BookResponse.class).getBody();

        BookRequest update = new BookRequest();
        update.setTitle("Atualizado");
        update.setAuthor("Novo Autor");
        update.setStatus(ReadingStatus.READING);
        update.setRating(5);

        ResponseEntity<BookResponse> response = restTemplate.exchange(
            "/api/books/" + created.getId(), HttpMethod.PUT,
            new HttpEntity<>(update, authHeaders()), BookResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getTitle()).isEqualTo("Atualizado");
        assertThat(response.getBody().getStatus()).isEqualTo(ReadingStatus.READING);
    }

    @Test
    @DisplayName("RF-006: Deve excluir livro com sucesso")
    void shouldDeleteBook() {
        BookRequest create = new BookRequest(); create.setTitle("Para Excluir"); create.setAuthor("Autor");
        BookResponse created = restTemplate.exchange("/api/books", HttpMethod.POST,
            new HttpEntity<>(create, authHeaders()), BookResponse.class).getBody();

        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
            "/api/books/" + created.getId(), HttpMethod.DELETE,
            new HttpEntity<>(authHeaders()), Void.class);

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> getResponse = restTemplate.exchange(
            "/api/books/" + created.getId(), HttpMethod.GET,
            new HttpEntity<>(authHeaders()), String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
