package br.com.cidadeemfoco.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ForgotPasswordRequest(
        @NotBlank(message = "O email e obrigatorio")
        @Email(message = "Informe um email valido")
        @Size(max = 254, message = "O email deve ter no maximo 254 caracteres")
        String email
) {
}
