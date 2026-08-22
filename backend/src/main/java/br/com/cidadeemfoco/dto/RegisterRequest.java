package br.com.cidadeemfoco.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "O nome e obrigatorio")
        @Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres")
        String name,

        @NotBlank(message = "O email e obrigatorio")
        @Email(message = "Informe um email valido")
        @Size(max = 254, message = "O email deve ter no maximo 254 caracteres")
        String email,

        @NotBlank(message = "A senha e obrigatoria")
        @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres")
        String password
) {
}
