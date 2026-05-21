package com.biblioteca.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "books")
public class Book {

    @Id
    private String id;

    private String title;
    private String author;

    @Indexed(unique = true)
    private String isbn;

    private String genre;
    private Integer year;
    private String description;
    private ReadingStatus status;
    private Integer rating;
    private String userId;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    public enum ReadingStatus {
        TO_READ, READING, COMPLETED, ABANDONED
    }
}
