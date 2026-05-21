package com.biblioteca.dto.request;
import com.biblioteca.model.Book.ReadingStatus;
import jakarta.validation.constraints.*;
import lombok.Data;
@Data
public class BookRequest {
    @NotBlank(message = "Título é obrigatório") private String title;
    @NotBlank(message = "Autor é obrigatório") private String author;
    private String isbn;
    private String genre;
    @Min(1000) @Max(2100) private Integer year;
    private String description;
    private ReadingStatus status;
    @Min(1) @Max(5) private Integer rating;
}
