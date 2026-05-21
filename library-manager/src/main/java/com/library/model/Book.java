package com.library.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
    private String isbn;
    private String genre;
    private String description;

    @Builder.Default
    private ReadingStatus status = ReadingStatus.WANT_TO_READ;

    private Integer rating; // 1-5

    private String notes;

    @Indexed
    private String userId;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    public enum ReadingStatus {
        WANT_TO_READ,
        READING,
        READ
    }
}
