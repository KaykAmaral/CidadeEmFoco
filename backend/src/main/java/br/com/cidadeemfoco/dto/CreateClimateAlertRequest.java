package br.com.cidadeemfoco.dto;

import br.com.cidadeemfoco.enums.AlertSeverity;
import br.com.cidadeemfoco.enums.ClimateAlertType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record CreateClimateAlertRequest(
        @NotBlank(message = "O titulo e obrigatorio")
        @Size(max = 150, message = "O titulo deve ter no maximo 150 caracteres")
        String title,

        @NotNull(message = "O tipo e obrigatorio")
        ClimateAlertType type,

        @NotNull(message = "A severidade e obrigatoria")
        AlertSeverity severity,

        @NotBlank(message = "A descricao e obrigatoria")
        @Size(max = 2000, message = "A descricao deve ter no maximo 2000 caracteres")
        String description,

        @NotNull(message = "O inicio da validade e obrigatorio")
        Instant startAt,

        @NotNull(message = "O fim da validade e obrigatorio")
        Instant endAt
) {
}
