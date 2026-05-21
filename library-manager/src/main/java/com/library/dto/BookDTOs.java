package com.library.dto;

import com.library.model.Book;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

public class BookDTOs {

    @Data
    public static class BookRequest {
        @NotBlank(message = "Título é obrigatório")
        private String title;

        @NotBlank(message = "Autor é obrigatório")
        private String author;

        private String isbn;
        private String genre;
        private String description;
        private Book.ReadingStatus status;

        @Min(value = 1, message = "Avaliação mínima é 1")
        @Max(value = 5, message = "Avaliação máxima é 5")
        private Integer rating;

        private String notes;
    }

    @Data
    public static class BookResponse {
        private String id;
        private String title;
        private String author;
        private String isbn;
        private String genre;
        private String description;
        private Book.ReadingStatus status;
        private Integer rating;
        private String notes;
        private String userId;
        private String createdAt;
        private String updatedAt;

        public static BookResponse from(Book book) {
            BookResponse r = new BookResponse();
            r.setId(book.getId());
            r.setTitle(book.getTitle());
            r.setAuthor(book.getAuthor());
            r.setIsbn(book.getIsbn());
            r.setGenre(book.getGenre());
            r.setDescription(book.getDescription());
            r.setStatus(book.getStatus());
            r.setRating(book.getRating());
            r.setNotes(book.getNotes());
            r.setUserId(book.getUserId());
            r.setCreatedAt(book.getCreatedAt() != null ? book.getCreatedAt().toString() : null);
            r.setUpdatedAt(book.getUpdatedAt() != null ? book.getUpdatedAt().toString() : null);
            return r;
        }
    }

    @Data
    public static class OpenLibraryBookDTO {
        private String title;
        private String author;
        private String isbn;
        private String coverUrl;
        private String description;
        private Integer publishYear;
    }
}
