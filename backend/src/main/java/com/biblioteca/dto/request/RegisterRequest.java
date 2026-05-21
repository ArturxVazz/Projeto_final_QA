package com.biblioteca.dto.request;
import jakarta.validation.constraints.*;
import lombok.Data;
@Data
public class RegisterRequest {
    @NotBlank(message = "Username é obrigatório") @Size(min = 3, max = 50)
    private String username;
    @NotBlank(message = "Email é obrigatório") @Email(message = "Email inválido")
    private String email;
    @NotBlank(message = "Senha é obrigatória") @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
    private String password;
}
