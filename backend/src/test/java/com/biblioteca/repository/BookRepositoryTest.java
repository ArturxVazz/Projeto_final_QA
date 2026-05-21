package com.biblioteca.repository;

import com.biblioteca.integration.BaseIntegrationTest;
import com.biblioteca.model.Book;
import com.biblioteca.model.Book.ReadingStatus;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BookRepository - Testes com Testcontainers (MongoDB Real)")
class BookRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private BookRepository bookRepository;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();
    }

    @Test
    @DisplayName("findByUserId: deve retornar apenas livros do usuário")
    void shouldFindOnlyUserBooks() {
        bookRepository.saveAll(List.of(
            Book.builder().title("L1").author("A").userId("user-1").status(ReadingStatus.TO_READ).build(),
            Book.builder().title("L2").author("A").userId("user-1").status(ReadingStatus.READING).build(),
            Book.builder().title("L3").author("A").userId("user-2").status(ReadingStatus.TO_READ).build()
        ));

        List<Book> result = bookRepository.findByUserId("user-1");

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(b -> b.getUserId().equals("user-1"));
    }

    @Test
    @DisplayName("findByUserIdAndStatus: deve filtrar por status corretamente")
    void shouldFilterByStatus() {
        bookRepository.saveAll(List.of(
            Book.builder().title("Lido").author("A").userId("u1").status(ReadingStatus.COMPLETED).build(),
            Book.builder().title("Lendo").author("A").userId("u1").status(ReadingStatus.READING).build()
        ));

        List<Book> completed = bookRepository.findByUserIdAndStatus("u1", ReadingStatus.COMPLETED);
        assertThat(completed).hasSize(1).allMatch(b -> b.getStatus() == ReadingStatus.COMPLETED);
    }

    @Test
    @DisplayName("findByIsbn: deve encontrar livro por ISBN único")
    void shouldFindByIsbn() {
        bookRepository.save(Book.builder().title("Livro").author("A")
            .isbn("978-123").userId("u1").status(ReadingStatus.TO_READ).build());

        Optional<Book> found = bookRepository.findByIsbn("978-123");
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Livro");
    }

    @Test
    @DisplayName("findByUserIdAndTitleContainingIgnoreCase: deve buscar insensível a maiúsculas")
    void shouldSearchTitleCaseInsensitive() {
        bookRepository.saveAll(List.of(
            Book.builder().title("Dom Casmurro").author("Machado").userId("u1").status(ReadingStatus.TO_READ).build(),
            Book.builder().title("O Cortiço").author("Aluísio").userId("u1").status(ReadingStatus.TO_READ).build()
        ));

        List<Book> result = bookRepository.findByUserIdAndTitleContainingIgnoreCase("u1", "dom");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Dom Casmurro");
    }
}
