package com.biblioteca.service;

import com.biblioteca.dto.request.BookRequest;
import com.biblioteca.dto.response.BookResponse;
import com.biblioteca.exception.*;
import com.biblioteca.model.Book;
import com.biblioteca.model.Book.ReadingStatus;
import com.biblioteca.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;

    public BookResponse create(BookRequest request, String userId) {
        if (request.getIsbn() != null && bookRepository.existsByIsbnAndUserId(request.getIsbn(), userId)) {
            throw new DuplicateResourceException("Livro com ISBN " + request.getIsbn() + " já cadastrado");
        }
        Book book = toEntity(request, userId);
        return toResponse(bookRepository.save(book));
    }

    public BookResponse findById(String id, String userId) {
        Book book = bookRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Livro não encontrado: " + id));
        validateOwnership(book, userId);
        return toResponse(book);
    }

    public List<BookResponse> findAllByUser(String userId) {
        return bookRepository.findByUserId(userId).stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<BookResponse> findByStatus(String userId, ReadingStatus status) {
        return bookRepository.findByUserIdAndStatus(userId, status).stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<BookResponse> searchByTitle(String userId, String title) {
        return bookRepository.findByUserIdAndTitleContainingIgnoreCase(userId, title).stream().map(this::toResponse).collect(Collectors.toList());
    }

    public BookResponse update(String id, BookRequest request, String userId) {
        Book existing = bookRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Livro não encontrado: " + id));
        validateOwnership(existing, userId);

        existing.setTitle(request.getTitle());
        existing.setAuthor(request.getAuthor());
        existing.setIsbn(request.getIsbn());
        existing.setGenre(request.getGenre());
        existing.setYear(request.getYear());
        existing.setDescription(request.getDescription());
        existing.setStatus(request.getStatus());
        existing.setRating(request.getRating());
        existing.setUpdatedAt(LocalDateTime.now());

        return toResponse(bookRepository.save(existing));
    }

    public void delete(String id, String userId) {
        Book book = bookRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Livro não encontrado: " + id));
        validateOwnership(book, userId);
        bookRepository.delete(book);
    }

    private void validateOwnership(Book book, String userId) {
        if (!book.getUserId().equals(userId)) {
            throw new UnauthorizedException("Você não tem permissão para acessar este livro");
        }
    }

    private Book toEntity(BookRequest req, String userId) {
        return Book.builder()
            .title(req.getTitle())
            .author(req.getAuthor())
            .isbn(req.getIsbn())
            .genre(req.getGenre())
            .year(req.getYear())
            .description(req.getDescription())
            .status(req.getStatus() != null ? req.getStatus() : ReadingStatus.TO_READ)
            .rating(req.getRating())
            .userId(userId)
            .build();
    }

    private BookResponse toResponse(Book book) {
        return BookResponse.builder()
            .id(book.getId())
            .title(book.getTitle())
            .author(book.getAuthor())
            .isbn(book.getIsbn())
            .genre(book.getGenre())
            .year(book.getYear())
            .description(book.getDescription())
            .status(book.getStatus())
            .rating(book.getRating())
            .createdAt(book.getCreatedAt())
            .updatedAt(book.getUpdatedAt())
            .build();
    }
}
