package br.com.cidadeemfoco.dto;

import br.com.cidadeemfoco.enums.OccurrenceStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOccurrenceStatusRequest(
        @NotNull(message = "O status e obrigatorio")
        OccurrenceStatus status
) {
}
