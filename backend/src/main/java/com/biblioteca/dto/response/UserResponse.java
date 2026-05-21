package com.biblioteca.dto.response;
import lombok.*;
import java.time.LocalDateTime;
@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class UserResponse {
    private String id;
    private String username;
    private String email;
    private LocalDateTime createdAt;
}
