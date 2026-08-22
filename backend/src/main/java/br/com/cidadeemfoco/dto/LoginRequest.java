package br.com.cidadeemfoco.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "O email e obrigatorio")
        @Email(message = "Informe um email valido")
        String email,

        @NotBlank(message = "A senha e obrigatoria")
        String password
) {
}
