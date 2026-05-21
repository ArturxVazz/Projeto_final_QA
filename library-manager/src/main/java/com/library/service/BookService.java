package com.library.service;

import com.library.dto.BookDTOs;
import com.library.model.Book;
import com.library.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<Book> findAllByUser(String userId) {
        return bookRepository.findByUserId(userId);
    }

    public List<Book> findByUserAndStatus(String userId, Book.ReadingStatus status) {
        return bookRepository.findByUserIdAndStatus(userId, status);
    }

    public List<Book> searchByTitle(String userId, String title) {
        return bookRepository.findByUserIdAndTitleContainingIgnoreCase(userId, title);
    }

    public Book findByIdAndUser(String bookId, String userId) {
        return bookRepository.findByIdAndUserId(bookId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Livro não encontrado"));
    }

    public Book create(BookDTOs.BookRequest request, String userId) {
        Book book = Book.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .isbn(request.getIsbn())
                .genre(request.getGenre())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : Book.ReadingStatus.WANT_TO_READ)
                .rating(request.getRating())
                .notes(request.getNotes())
                .userId(userId)
                .build();

        return bookRepository.save(book);
    }

    public Book update(String bookId, BookDTOs.BookRequest request, String userId) {
        Book book = findByIdAndUser(bookId, userId);

        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());
        book.setGenre(request.getGenre());
        book.setDescription(request.getDescription());
        if (request.getStatus() != null) book.setStatus(request.getStatus());
        book.setRating(request.getRating());
        book.setNotes(request.getNotes());
        book.setUpdatedAt(LocalDateTime.now());

        return bookRepository.save(book);
    }

    public void delete(String bookId, String userId) {
        Book book = findByIdAndUser(bookId, userId);
        bookRepository.delete(book);
    }

    public Map<String, Long> getStats(String userId) {
        long total = bookRepository.countByUserId(userId);
        long read = bookRepository.countByUserIdAndStatus(userId, Book.ReadingStatus.READ);
        long reading = bookRepository.countByUserIdAndStatus(userId, Book.ReadingStatus.READING);
        long wantToRead = bookRepository.countByUserIdAndStatus(userId, Book.ReadingStatus.WANT_TO_READ);

        return Map.of(
                "total", total,
                "read", read,
                "reading", reading,
                "wantToRead", wantToRead
        );
    }

    public int calculateCompletionPercentage(long read, long total) {
        if (total == 0) return 0;
        return (int) Math.round((double) read / total * 100);
    }
}
