package com.biblioteca.dto.response;
import lombok.*;
@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class AuthResponse {
    private String token;
    private String type;
    private String username;
    private String email;
}
