package com.library.library_manager.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthDtos {
    public static class ResgisterRequest{

        @NotBlank
        @Size(min = 3, max = 50, message = "User deve ter entre 3 e 50 caracteres")
        private String username;


        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email invalido")
        private String email;

        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
        private String password;

        // Getters e Setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

    }

    public static class LoginRequest {

        @NotBlank(message = "Username é obrigatório")
        private String username;

        @NotBlank(message = "Senha é obrigatória")
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class AuthResponse {

        private String token;
        private String username;
        private String userId;

        public AuthResponse(String token, String username, String userId) {
            this.token = token;
            this.username = username;
            this.userId = userId;
        }

        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
    }
}
