package com.biblioteca.dto.response;
import com.biblioteca.model.Book.ReadingStatus;
import lombok.*;
import java.time.LocalDateTime;
@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class BookResponse {
    private String id;
    private String title;
    private String author;
    private String isbn;
    private String genre;
    private Integer year;
    private String description;
    private ReadingStatus status;
    private Integer rating;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
