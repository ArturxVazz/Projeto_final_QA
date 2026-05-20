package com.library.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

public class AuthDTOs {

    @Data
    public static class RegisterRequest {
        @NotBlank(message = "Username é obrigatório")
        @Size(min = 3, max = 50, message = "Username deve ter entre 3 e 50 caracteres")
        private String username;

        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email inválido")
        private String email;

        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
        private String password;
    }

    @Data
    public static class LoginRequest {
        @NotBlank(message = "Username é obrigatório")
        private String username;

        @NotBlank(message = "Senha é obrigatória")
        private String password;
    }

    @Data
    public static class AuthResponse {
        private String token;
        private String username;
        private String userId;
        private String message;

        public AuthResponse(String token, String username, String userId) {
            this.token = token;
            this.username = username;
            this.userId = userId;
        }
    }
}
