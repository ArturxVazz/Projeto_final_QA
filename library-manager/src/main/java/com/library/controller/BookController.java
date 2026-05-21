package com.library.controller;

import com.library.dto.BookDTOs;
import com.library.model.Book;
import com.library.service.BookService;
import com.library.service.OpenLibraryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;
    private final OpenLibraryService openLibraryService;

    public BookController(BookService bookService, OpenLibraryService openLibraryService) {
        this.bookService = bookService;
        this.openLibraryService = openLibraryService;
    }

    @GetMapping
    public ResponseEntity<List<BookDTOs.BookResponse>> listBooks(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            Authentication auth) {

        String userId = (String) auth.getCredentials();
        List<Book> books;

        if (search != null && !search.isBlank()) {
            books = bookService.searchByTitle(userId, search);
        } else if (status != null) {
            books = bookService.findByUserAndStatus(userId, Book.ReadingStatus.valueOf(status));
        } else {
            books = bookService.findAllByUser(userId);
        }

        List<BookDTOs.BookResponse> response = books.stream()
                .map(BookDTOs.BookResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBook(@PathVariable String id, Authentication auth) {
        try {
            String userId = (String) auth.getCredentials();
            Book book = bookService.findByIdAndUser(id, userId);
            return ResponseEntity.ok(BookDTOs.BookResponse.from(book));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createBook(@Valid @RequestBody BookDTOs.BookRequest request,
                                        Authentication auth) {
        String userId = (String) auth.getCredentials();
        Book book = bookService.create(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(BookDTOs.BookResponse.from(book));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBook(@PathVariable String id,
                                        @Valid @RequestBody BookDTOs.BookRequest request,
                                        Authentication auth) {
        try {
            String userId = (String) auth.getCredentials();
            Book book = bookService.update(id, request, userId);
            return ResponseEntity.ok(BookDTOs.BookResponse.from(book));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBook(@PathVariable String id, Authentication auth) {
        try {
            String userId = (String) auth.getCredentials();
            bookService.delete(id, userId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats(Authentication auth) {
        String userId = (String) auth.getCredentials();
        return ResponseEntity.ok(bookService.getStats(userId));
    }

    @GetMapping("/search/openlibrary")
    public ResponseEntity<?> searchOpenLibrary(@RequestParam String q, Authentication auth) {
        try {
            List<BookDTOs.OpenLibraryBookDTO> results = openLibraryService.searchBooks(q);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("message", "Serviço externo indisponível: " + e.getMessage()));
        }
    }
}
