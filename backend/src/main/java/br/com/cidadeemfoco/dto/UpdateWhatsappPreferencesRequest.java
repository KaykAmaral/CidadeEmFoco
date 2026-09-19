package br.com.cidadeemfoco.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateWhatsappPreferencesRequest(
        @NotBlank(message = "O numero do WhatsApp e obrigatorio")
        @Size(max = 30, message = "O numero do WhatsApp deve ter no maximo 30 caracteres")
        String phoneNumber,

        @AssertTrue(message = "E necessario autorizar o recebimento de alertas pelo WhatsApp")
        boolean consentGiven
) {
}
