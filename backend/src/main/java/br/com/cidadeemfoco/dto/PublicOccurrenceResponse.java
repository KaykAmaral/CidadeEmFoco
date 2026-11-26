package br.com.cidadeemfoco.dto;

import br.com.cidadeemfoco.entity.Occurrence;
import br.com.cidadeemfoco.enums.OccurrenceCategory;
import br.com.cidadeemfoco.enums.OccurrenceStatus;
import br.com.cidadeemfoco.enums.OccurrenceType;
import br.com.cidadeemfoco.enums.PerceivedRisk;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Resumo público sem descrição livre, endereço, coordenadas, imagens ou dados do cidadão")
public record PublicOccurrenceResponse(
        @Schema(example = "42") Long id,
        @Schema(example = "EVENTO_NATURAL") OccurrenceCategory category,
        @Schema(example = "ALAGAMENTO") OccurrenceType type,
        @Schema(example = "ALTO") PerceivedRisk perceivedRisk,
        @Schema(example = "Boqueirao") String neighborhood,
        @Schema(example = "EM_ATENDIMENTO") OccurrenceStatus status,
        @Schema(example = "2026-10-01T12:00:00Z") Instant createdAt,
        Instant resolvedAt,
        @Schema(example = "3") int reports
) {
    public static PublicOccurrenceResponse from(Occurrence occurrence) {
        Occurrence caseRoot = occurrence.getCaseRoot();
        return new PublicOccurrenceResponse(
                caseRoot.getId(),
                caseRoot.getCategory(),
                caseRoot.getType(),
                caseRoot.getPerceivedRisk(),
                caseRoot.getNeighborhood(),
                caseRoot.getStatus(),
                caseRoot.getCreatedAt(),
                caseRoot.getResolvedAt(),
                caseRoot.getStrength()
        );
    }
}
