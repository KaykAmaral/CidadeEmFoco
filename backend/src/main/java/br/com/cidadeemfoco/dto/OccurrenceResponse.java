package br.com.cidadeemfoco.dto;

import br.com.cidadeemfoco.entity.Occurrence;
import br.com.cidadeemfoco.enums.OccurrenceCategory;
import br.com.cidadeemfoco.enums.OccurrenceStatus;
import br.com.cidadeemfoco.enums.OccurrenceType;
import br.com.cidadeemfoco.enums.PerceivedRisk;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

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
        List<String> imageUrls,
        OccurrenceStatus status,
        Instant createdAt,
        Instant updatedAt,
        Instant resolvedAt
) {

    public OccurrenceResponse(
            Long id, OccurrenceCategory category, OccurrenceType type, String description,
            PerceivedRisk perceivedRisk, BigDecimal latitude, BigDecimal longitude,
            String neighborhood, String address, String imageUrl, OccurrenceStatus status,
            Instant createdAt, Instant updatedAt
    ) {
        this(id, category, type, description, perceivedRisk, latitude, longitude,
                neighborhood, address, imageUrl,
                imageUrl == null ? List.of() : List.of(imageUrl), status, createdAt, updatedAt, null);
    }

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
                occurrence.getImages().isEmpty() ? null : "/api/occurrences/" + occurrence.getId() + "/images/" + occurrence.getImages().getFirst().getId(),
                occurrence.getImages().stream().map(image -> "/api/occurrences/" + occurrence.getId() + "/images/" + image.getId()).toList(),
                occurrence.getStatus(),
                occurrence.getCreatedAt(),
                occurrence.getUpdatedAt(),
                occurrence.getResolvedAt()
        );
    }
}
