package br.com.cidadeemfoco.dto;

import br.com.cidadeemfoco.entity.Occurrence;
import br.com.cidadeemfoco.enums.OccurrenceCategory;
import br.com.cidadeemfoco.enums.OccurrenceStatus;
import br.com.cidadeemfoco.enums.OccurrenceType;
import br.com.cidadeemfoco.enums.PerceivedRisk;

import java.math.BigDecimal;
import java.time.Instant;

public record OccurrenceResponse(
        Long id,
        OccurrenceCategory category,
        OccurrenceType type,
        String description,
        PerceivedRisk perceivedRisk,
        BigDecimal latitude,
        BigDecimal longitude,
        String neighborhood,
        String address,
        String imageUrl,
        OccurrenceStatus status,
        Instant createdAt,
        Instant updatedAt
) {

    public static OccurrenceResponse from(Occurrence occurrence) {
        return new OccurrenceResponse(
                occurrence.getId(),
                occurrence.getCategory(),
                occurrence.getType(),
                occurrence.getDescription(),
                occurrence.getPerceivedRisk(),
                occurrence.getLatitude(),
                occurrence.getLongitude(),
                occurrence.getNeighborhood(),
                occurrence.getAddress(),
                occurrence.getImagePath() == null
                        ? null
                        : "/api/occurrences/" + occurrence.getId() + "/image",
                occurrence.getStatus(),
                occurrence.getCreatedAt(),
                occurrence.getUpdatedAt()
        );
    }
}
