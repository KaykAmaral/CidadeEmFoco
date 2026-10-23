package br.com.cidadeemfoco.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserProfileRequest(
        @NotBlank(message = "O nome e obrigatorio")
        @Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres")
        String name,

        @NotBlank(message = "O CPF e obrigatorio")
        @Size(max = 14, message = "O CPF deve ter no maximo 14 caracteres")
        String cpf,

        @NotBlank(message = "O numero do WhatsApp e obrigatorio")
        @Size(max = 30, message = "O numero do WhatsApp deve ter no maximo 30 caracteres")
        String whatsappPhone,

        @AssertTrue(message = "E necessario autorizar o recebimento de alertas pelo WhatsApp")
        boolean whatsappConsentGiven,

        @NotBlank(message = "O logradouro e obrigatorio")
        @Size(max = 150, message = "O logradouro deve ter no maximo 150 caracteres")
        String street,

        @NotBlank(message = "O numero do endereco e obrigatorio")
        @Size(max = 20, message = "O numero do endereco deve ter no maximo 20 caracteres")
        String addressNumber,

        @Size(max = 100, message = "O complemento deve ter no maximo 100 caracteres")
        String addressComplement,

        @NotBlank(message = "O bairro e obrigatorio")
        @Size(max = 100, message = "O bairro deve ter no maximo 100 caracteres")
        String neighborhood,

        @NotBlank(message = "O CEP e obrigatorio")
        @Size(max = 9, message = "O CEP deve ter no maximo 9 caracteres")
        String postalCode,

        @NotBlank(message = "A cidade e obrigatoria")
        @Size(min = 7, max = 7, message = "Informe um codigo IBGE valido")
        String cityIbgeCode
) {
}
