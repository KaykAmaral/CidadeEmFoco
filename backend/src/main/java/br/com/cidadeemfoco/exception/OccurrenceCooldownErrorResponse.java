package br.com.cidadeemfoco.exception;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;

@Schema(description = "Erro retornado durante o intervalo minimo entre ocorrencias")
public record OccurrenceCooldownErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors,
        @Schema(description = "Segundos restantes ate um novo registro", example = "600")
        long retryAfterSeconds,
        @Schema(description = "Instante em que um novo registro sera permitido")
        Instant nextAllowedAt
) {
}
