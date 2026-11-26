package br.com.cidadeemfoco.dto;

import java.time.LocalDate;

public record DailyOccurrenceMetricsResponse(
        LocalDate date,
        long registeredCases,
        long resolvedCases
) {
}
