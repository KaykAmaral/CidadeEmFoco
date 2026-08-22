package br.com.cidadeemfoco.dto;

import br.com.cidadeemfoco.enums.OccurrenceCategory;
import br.com.cidadeemfoco.enums.OccurrenceType;
import br.com.cidadeemfoco.enums.PerceivedRisk;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateOccurrenceRequest(
        @NotNull(message = "A categoria e obrigatoria")
        OccurrenceCategory category,

        @NotNull(message = "O tipo e obrigatorio")
        OccurrenceType type,

        @NotBlank(message = "A descricao e obrigatoria")
        @Size(max = 2000, message = "A descricao deve ter no maximo 2000 caracteres")
        String description,

        @NotNull(message = "O risco percebido e obrigatorio")
        PerceivedRisk perceivedRisk,

        @NotNull(message = "A latitude e obrigatoria")
        @DecimalMin(value = "-90", message = "A latitude deve ser maior ou igual a -90")
        @DecimalMax(value = "90", message = "A latitude deve ser menor ou igual a 90")
        BigDecimal latitude,

        @NotNull(message = "A longitude e obrigatoria")
        @DecimalMin(value = "-180", message = "A longitude deve ser maior ou igual a -180")
        @DecimalMax(value = "180", message = "A longitude deve ser menor ou igual a 180")
        BigDecimal longitude,

        @Size(max = 100, message = "O bairro deve ter no maximo 100 caracteres")
        String neighborhood,

        @Size(max = 255, message = "O endereco deve ter no maximo 255 caracteres")
        String address
) {
}
