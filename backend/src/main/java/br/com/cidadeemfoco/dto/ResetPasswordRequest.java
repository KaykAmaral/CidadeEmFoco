package br.com.cidadeemfoco.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank(message = "O token e obrigatorio")
        @Size(max = 200, message = "O token informado e invalido")
        String token,

        @NotBlank(message = "A nova senha e obrigatoria")
        @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres")
        String newPassword
) {
}
