package br.com.cidadeemfoco.exception;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;

@Schema(description = "Formato padronizado de erro da API")
public record ApiErrorResponse(
        @Schema(example = "2026-10-01T12:00:00Z")
        Instant timestamp,
        @Schema(example = "400")
        int status,
        @Schema(example = "Bad Request")
        String error,
        @Schema(example = "Requisicao invalida")
        String message,
        @Schema(example = "/api/v1/public/occurrences")
        String path,
        @Schema(description = "Erros por campo; vazio quando não aplicável", example = "{}")
        Map<String, String> fieldErrors
) {
}
