package br.com.cidadeemfoco.dto;

import br.com.cidadeemfoco.enums.OccurrenceCategory;
import br.com.cidadeemfoco.enums.OccurrenceStatus;
import br.com.cidadeemfoco.enums.OccurrenceType;

public record OccurrenceFilter(
        OccurrenceCategory category,
        OccurrenceType type,
        OccurrenceStatus status,
        String neighborhood
) {
}
