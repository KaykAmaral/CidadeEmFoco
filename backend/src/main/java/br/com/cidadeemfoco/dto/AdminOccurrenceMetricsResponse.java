package br.com.cidadeemfoco.dto;

import br.com.cidadeemfoco.enums.OccurrenceCategory;
import br.com.cidadeemfoco.enums.OccurrenceStatus;
import br.com.cidadeemfoco.enums.OccurrenceType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public record AdminOccurrenceMetricsResponse(
        CityResponse city,
        Instant createdFrom,
        Instant createdTo,
        long totalCases,
        long totalReports,
        long openCases,
        long resolvedCases,
        long notConfirmedCases,
        long automaticallyResolvedCases,
        BigDecimal resolutionRatePercent,
        Long averageResolutionTimeMinutes,
        Map<OccurrenceStatus, Long> casesByStatus,
        Map<OccurrenceCategory, Long> casesByCategory,
        Map<OccurrenceType, Long> casesByType,
        List<NamedCountResponse> casesByNeighborhood,
        List<DailyOccurrenceMetricsResponse> dailyEvolution
) {
}
