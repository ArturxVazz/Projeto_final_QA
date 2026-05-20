package com.library.library_manager.dto;

import com.library.library_manager.model.Book;
import com.library.library_manager.model.ReadingStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class BookDtos {
    public static class BookRequest {

        @NotBlank(message = "Título é obrigatório")
        private String title;

        @NotBlank(message = "Autor é obrigatório")
        private String author;

        private String isbn;
        private String genre;
        private String description;

        private ReadingStatus status;

        @Min(value = 1, message = "Avaliação mínima é 1")
        @Max(value = 5, message = "Avaliação máxima é 5")
        private Integer rating;

        private String notes;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getAuthor() { return author; }
        public void setAuthor(String author) { this.author = author; }

        public String getIsbn() { return isbn; }
        public void setIsbn(String isbn) { this.isbn = isbn; }

        public String getGenre() { return genre; }
        public void setGenre(String genre) { this.genre = genre; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public ReadingStatus getStatus() { return status; }
        public void setStatus(ReadingStatus status) { this.status = status; }

        public Integer getRating() { return rating; }
        public void setRating(Integer rating) { this.rating = rating; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    public static class BookResponse {

        private String id;
        private String title;
        private String author;
        private String isbn;
        private String genre;
        private String description;
        private ReadingStatus status;
        private Integer rating;
        private String notes;
        private String userId;
        private String createdAt;
        private String updatedAt;

        public static BookResponse from(Book book) {
            BookResponse r = new BookResponse();
            r.id = book.getId();
            r.title = book.getTitle();
            r.author = book.getAuthor();
            r.isbn = book.getIsbn();
            r.genre = book.getGenre();
            r.description = book.getDescription();
            r.status = book.getStatus();
            r.rating = book.getRating();
            r.notes = book.getNotes();
            r.userId = book.getUserId();
            r.createdAt = book.getCreatedAt() != null ? book.getCreatedAt().toString() : null;
            r.updatedAt = book.getUpdateAt() != null ? book.getUpdateAt().toString() : null;
            return r;
        }

        // Getters e Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getAuthor() { return author; }
        public void setAuthor(String author) { this.author = author; }

        public String getIsbn() { return isbn; }
        public void setIsbn(String isbn) { this.isbn = isbn; }

        public String getGenre() { return genre; }
        public void setGenre(String genre) { this.genre = genre; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public ReadingStatus getStatus() { return status; }
        public void setStatus(ReadingStatus status) { this.status = status; }

        public Integer getRating() { return rating; }
        public void setRating(Integer rating) { this.rating = rating; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

        public String getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    }

    public static class OpenLibraryBookDTO {
        private String title;
        private String author;
        private String isbn;
        private Integer publishYear;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getAuthor() { return author; }
        public void setAuthor(String author) { this.author = author; }

        public String getIsbn() { return isbn; }
        public void setIsbn(String isbn) { this.isbn = isbn; }

        public Integer getPublishYear() { return publishYear; }
        public void setPublishYear(Integer publishYear) { this.publishYear = publishYear; }
    }
}
