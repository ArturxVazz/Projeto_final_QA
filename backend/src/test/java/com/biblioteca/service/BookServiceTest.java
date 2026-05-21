package com.biblioteca.service;

import com.biblioteca.dto.request.BookRequest;
import com.biblioteca.dto.response.BookResponse;
import com.biblioteca.exception.*;
import com.biblioteca.model.Book;
import com.biblioteca.model.Book.ReadingStatus;
import com.biblioteca.repository.BookRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookService - Testes Unitários (Caixa Branca)")
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    private static final String USER_ID = "user-123";
    private static final String BOOK_ID = "book-456";

    private Book buildBook(String id, String title, String userId) {
        return Book.builder()
            .id(id).title(title).author("Autor").userId(userId)
            .status(ReadingStatus.TO_READ).createdAt(LocalDateTime.now())
            .build();
    }

    private BookRequest buildRequest(String title) {
        BookRequest req = new BookRequest();
        req.setTitle(title);
        req.setAuthor("Autor");
        return req;
    }

    @Test
    @DisplayName("create: deve criar livro e retornar response")
    void shouldCreateBook() {
        BookRequest req = buildRequest("Novo Livro");
        Book saved = buildBook(BOOK_ID, "Novo Livro", USER_ID);
        when(bookRepository.save(any(Book.class))).thenReturn(saved);

        BookResponse result = bookService.create(req, USER_ID);

        assertThat(result.getId()).isEqualTo(BOOK_ID);
        assertThat(result.getTitle()).isEqualTo("Novo Livro");
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    @DisplayName("create: deve lançar DuplicateResourceException para ISBN duplicado")
    void shouldThrowOnDuplicateIsbn() {
        BookRequest req = buildRequest("Livro");
        req.setIsbn("123-456");
        when(bookRepository.existsByIsbnAndUserId("123-456", USER_ID)).thenReturn(true);

        assertThatThrownBy(() -> bookService.create(req, USER_ID))
            .isInstanceOf(DuplicateResourceException.class)
            .hasMessageContaining("123-456");
    }

    @Test
    @DisplayName("findById: deve retornar livro existente do usuário correto")
    void shouldFindBookById() {
        Book book = buildBook(BOOK_ID, "Meu Livro", USER_ID);
        when(bookRepository.findById(BOOK_ID)).thenReturn(Optional.of(book));

        BookResponse result = bookService.findById(BOOK_ID, USER_ID);

        assertThat(result.getId()).isEqualTo(BOOK_ID);
        assertThat(result.getTitle()).isEqualTo("Meu Livro");
    }

    @Test
    @DisplayName("findById: deve lançar ResourceNotFoundException para ID inexistente")
    void shouldThrowWhenBookNotFound() {
        when(bookRepository.findById(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.findById("invalid", USER_ID))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("findById: deve lançar UnauthorizedException quando usuário não é dono")
    void shouldThrowWhenNotOwner() {
        Book book = buildBook(BOOK_ID, "Livro Alheio", "outro-user");
        when(bookRepository.findById(BOOK_ID)).thenReturn(Optional.of(book));

        assertThatThrownBy(() -> bookService.findById(BOOK_ID, USER_ID))
            .isInstanceOf(UnauthorizedException.class);
    }

    @ParameterizedTest(name = "Status: {0}")
    @EnumSource(ReadingStatus.class)
    @DisplayName("findByStatus: deve filtrar por todos os status de leitura")
    void shouldFindByAllStatuses(ReadingStatus status) {
        Book book = buildBook(BOOK_ID, "Livro", USER_ID);
        book.setStatus(status);
        when(bookRepository.findByUserIdAndStatus(USER_ID, status)).thenReturn(List.of(book));

        List<BookResponse> result = bookService.findByStatus(USER_ID, status);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(status);
    }

    @ParameterizedTest(name = "Rating: {0}")
    @ValueSource(ints = {1, 2, 3, 4, 5})
    @DisplayName("update: deve aceitar todos os ratings válidos (1-5)")
    void shouldAcceptAllValidRatings(int rating) {
        BookRequest req = buildRequest("Livro");
        req.setRating(rating);
        Book existing = buildBook(BOOK_ID, "Livro", USER_ID);
        Book updated = buildBook(BOOK_ID, "Livro", USER_ID);
        updated.setRating(rating);
        when(bookRepository.findById(BOOK_ID)).thenReturn(Optional.of(existing));
        when(bookRepository.save(any())).thenReturn(updated);

        BookResponse result = bookService.update(BOOK_ID, req, USER_ID);
        assertThat(result.getRating()).isEqualTo(rating);
    }

    @Test
    @DisplayName("delete: deve remover livro existente do dono")
    void shouldDeleteBook() {
        Book book = buildBook(BOOK_ID, "Livro", USER_ID);
        when(bookRepository.findById(BOOK_ID)).thenReturn(Optional.of(book));

        bookService.delete(BOOK_ID, USER_ID);

        verify(bookRepository).delete(book);
    }

    @Test
    @DisplayName("findAllByUser: deve retornar lista de livros do usuário")
    void shouldFindAllByUser() {
        List<Book> books = List.of(
            buildBook("1", "Livro A", USER_ID),
            buildBook("2", "Livro B", USER_ID),
            buildBook("3", "Livro C", USER_ID)
        );
        when(bookRepository.findByUserId(USER_ID)).thenReturn(books);

        List<BookResponse> result = bookService.findAllByUser(USER_ID);

        assertThat(result).hasSize(3);
    }
}
