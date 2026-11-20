package br.com.cidadeemfoco.dto;

import java.math.BigDecimal;

public record CityRankingEntryResponse(
        int position,
        CityResponse city,
        long totalCases,
        long confirmedCases,
        long resolvedCases,
        long notConfirmedCases,
        BigDecimal resolutionRatePercent
) {
}
