package com.library.service;

import com.library.dto.BookDTOs;
import com.library.model.Book;
import com.library.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@DisplayName("BookService - Testes Parametrizados")
class BookServiceParameterizedTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired BookService bookService;
    @Autowired BookRepository bookRepository;

    @BeforeEach
    void setUp() { bookRepository.deleteAll(); }

    @ParameterizedTest(name = "Status: {0}")
    @EnumSource(Book.ReadingStatus.class)
    @DisplayName("Deve criar livro com cada status de leitura")
    void shouldCreateBookWithAnyReadingStatus(Book.ReadingStatus status) {
        BookDTOs.BookRequest request = new BookDTOs.BookRequest();
        request.setTitle("Livro Teste");
        request.setAuthor("Autor");
        request.setStatus(status);

        Book saved = bookService.create(request, "user-param-test");

        assertThat(saved.getStatus()).isEqualTo(status);
        assertThat(saved.getId()).isNotNull();
    }

    @ParameterizedTest(name = "Percentual: {0}% lidos de {1} total = {2}%")
    @CsvSource({
        "0, 0, 0",
        "0, 10, 0",
        "5, 10, 50",
        "10, 10, 100",
        "1, 3, 33",
        "2, 3, 67",
        "3, 4, 75"
    })
    @DisplayName("Deve calcular percentual de conclusão corretamente")
    void shouldCalculateCompletionPercentageCorrectly(long read, long total, int expected) {
        int result = bookService.calculateCompletionPercentage(read, total);
        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest(name = "Busca por: \"{0}\"")
    @ValueSource(strings = {"clean", "CLEAN", "Clean", "cLEaN"})
    @DisplayName("Deve encontrar livros independente de capitalização")
    void shouldFindBooksCaseInsensitively(String searchTerm) {
        BookDTOs.BookRequest req = new BookDTOs.BookRequest();
        req.setTitle("Clean Code");
        req.setAuthor("Robert Martin");
        bookService.create(req, "user-ci-test");

        List<Book> results = bookService.searchByTitle("user-ci-test", searchTerm);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Clean Code");
    }

    @ParameterizedTest(name = "Título: \"{0}\", Autor: \"{1}\" - gênero: \"{2}\"")
    @CsvSource({
        "Dom Casmurro, Machado de Assis, Realismo",
        "O Alquimista, Paulo Coelho, Ficção",
        "1984, George Orwell, Distopia",
        "Duna, Frank Herbert, Ficção Científica",
        "Sapiens, Yuval Noah Harari, História"
    })
    @DisplayName("Deve persistir e recuperar livros com múltiplas combinações de dados")
    void shouldPersistAndRetrieveVariousBooks(String title, String author, String genre) {
        BookDTOs.BookRequest req = new BookDTOs.BookRequest();
        req.setTitle(title);
        req.setAuthor(author);
        req.setGenre(genre);

        Book saved = bookService.create(req, "user-multi");
        Book found = bookService.findByIdAndUser(saved.getId(), "user-multi");

        assertThat(found.getTitle()).isEqualTo(title);
        assertThat(found.getAuthor()).isEqualTo(author);
        assertThat(found.getGenre()).isEqualTo(genre);
    }

    @ParameterizedTest(name = "Avaliação {0} - deve ser salva corretamente")
    @ValueSource(ints = {1, 2, 3, 4, 5})
    @DisplayName("Deve aceitar e persistir todas as avaliações válidas (1–5)")
    void shouldAcceptAllValidRatings(int rating) {
        BookDTOs.BookRequest req = new BookDTOs.BookRequest();
        req.setTitle("Livro Avaliado");
        req.setAuthor("Autor");
        req.setRating(rating);
        req.setStatus(Book.ReadingStatus.READ);

        Book saved = bookService.create(req, "user-rating");

        assertThat(saved.getRating()).isEqualTo(rating);
    }
}
