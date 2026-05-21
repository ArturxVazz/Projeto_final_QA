package com.library.service;

import com.library.dto.BookDTOs;
import com.library.model.Book;
import com.library.repository.BookRepository;
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

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@DisplayName("BookService - Testes de Integração com Testcontainers")
class BookServiceTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired BookService bookService;
    @Autowired BookRepository bookRepository;

    private static final String USER_ID = "user-test-123";
    private static final String OTHER_USER_ID = "other-user-456";

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve criar livro com status padrão WANT_TO_READ quando status não informado")
    void shouldCreateBookWithDefaultStatus() {
        BookDTOs.BookRequest request = new BookDTOs.BookRequest();
        request.setTitle("Clean Code");
        request.setAuthor("Robert C. Martin");

        Book saved = bookService.create(request, USER_ID);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(Book.ReadingStatus.WANT_TO_READ);
        assertThat(saved.getUserId()).isEqualTo(USER_ID);
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Deve criar livro com status READING quando especificado")
    void shouldCreateBookWithReadingStatus() {
        BookDTOs.BookRequest request = new BookDTOs.BookRequest();
        request.setTitle("Domain-Driven Design");
        request.setAuthor("Eric Evans");
        request.setStatus(Book.ReadingStatus.READING);
        request.setGenre("Engenharia de Software");

        Book saved = bookService.create(request, USER_ID);

        assertThat(saved.getStatus()).isEqualTo(Book.ReadingStatus.READING);
        assertThat(saved.getGenre()).isEqualTo("Engenharia de Software");
    }

    @Test
    @DisplayName("Deve retornar todos os livros do usuário correto")
    void shouldReturnOnlyUserBooks() {
        createBookForUser("Livro A", USER_ID);
        createBookForUser("Livro B", USER_ID);
        createBookForUser("Livro C", OTHER_USER_ID);

        List<Book> books = bookService.findAllByUser(USER_ID);

        assertThat(books).hasSize(2);
        assertThat(books).allMatch(b -> b.getUserId().equals(USER_ID));
    }

    @Test
    @DisplayName("Deve filtrar livros por status READ")
    void shouldFilterByReadStatus() {
        createBookForUserWithStatus("Lido 1", USER_ID, Book.ReadingStatus.READ);
        createBookForUserWithStatus("Lido 2", USER_ID, Book.ReadingStatus.READ);
        createBookForUserWithStatus("Lendo", USER_ID, Book.ReadingStatus.READING);

        List<Book> readBooks = bookService.findByUserAndStatus(USER_ID, Book.ReadingStatus.READ);

        assertThat(readBooks).hasSize(2);
        assertThat(readBooks).allMatch(b -> b.getStatus() == Book.ReadingStatus.READ);
    }

    @Test
    @DisplayName("Deve buscar livros por título (case-insensitive)")
    void shouldSearchBooksByTitleCaseInsensitive() {
        createBookForUser("Harry Potter e a Pedra Filosofal", USER_ID);
        createBookForUser("harry potter e a câmara secreta", USER_ID);
        createBookForUser("Senhor dos Anéis", USER_ID);

        List<Book> results = bookService.searchByTitle(USER_ID, "harry potter");

        assertThat(results).hasSize(2);
    }

    @Test
    @DisplayName("Deve atualizar livro com novos dados")
    void shouldUpdateBookSuccessfully() {
        Book book = createBookForUser("Título Antigo", USER_ID);

        BookDTOs.BookRequest update = new BookDTOs.BookRequest();
        update.setTitle("Título Novo");
        update.setAuthor("Novo Autor");
        update.setStatus(Book.ReadingStatus.READ);
        update.setRating(5);

        Book updated = bookService.update(book.getId(), update, USER_ID);

        assertThat(updated.getTitle()).isEqualTo("Título Novo");
        assertThat(updated.getStatus()).isEqualTo(Book.ReadingStatus.READ);
        assertThat(updated.getRating()).isEqualTo(5);
        assertThat(updated.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar atualizar livro de outro usuário")
    void shouldThrowExceptionWhenUpdatingOtherUserBook() {
        Book book = createBookForUser("Livro Privado", USER_ID);

        BookDTOs.BookRequest update = new BookDTOs.BookRequest();
        update.setTitle("Invasão");
        update.setAuthor("Hacker");

        assertThatThrownBy(() -> bookService.update(book.getId(), update, OTHER_USER_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Livro não encontrado");
    }

    @Test
    @DisplayName("Deve deletar livro com sucesso")
    void shouldDeleteBookSuccessfully() {
        Book book = createBookForUser("Para Deletar", USER_ID);

        bookService.delete(book.getId(), USER_ID);

        assertThat(bookRepository.findByIdAndUserId(book.getId(), USER_ID)).isEmpty();
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar livro inexistente")
    void shouldThrowExceptionWhenDeletingNonExistentBook() {
        assertThatThrownBy(() -> bookService.delete("id-inexistente", USER_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Livro não encontrado");
    }

    @Test
    @DisplayName("Deve retornar estatísticas corretas do usuário")
    void shouldReturnCorrectStats() {
        createBookForUserWithStatus("L1", USER_ID, Book.ReadingStatus.READ);
        createBookForUserWithStatus("L2", USER_ID, Book.ReadingStatus.READ);
        createBookForUserWithStatus("L3", USER_ID, Book.ReadingStatus.READING);
        createBookForUserWithStatus("L4", USER_ID, Book.ReadingStatus.WANT_TO_READ);
        createBookForUser("Outro", OTHER_USER_ID); // não deve contar

        Map<String, Long> stats = bookService.getStats(USER_ID);

        assertThat(stats.get("total")).isEqualTo(4L);
        assertThat(stats.get("read")).isEqualTo(2L);
        assertThat(stats.get("reading")).isEqualTo(1L);
        assertThat(stats.get("wantToRead")).isEqualTo(1L);
    }

    // helpers
    private Book createBookForUser(String title, String userId) {
        BookDTOs.BookRequest req = new BookDTOs.BookRequest();
        req.setTitle(title);
        req.setAuthor("Autor Padrão");
        return bookService.create(req, userId);
    }

    private Book createBookForUserWithStatus(String title, String userId, Book.ReadingStatus status) {
        BookDTOs.BookRequest req = new BookDTOs.BookRequest();
        req.setTitle(title);
        req.setAuthor("Autor");
        req.setStatus(status);
        return bookService.create(req, userId);
    }
}
