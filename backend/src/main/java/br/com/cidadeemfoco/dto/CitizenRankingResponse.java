package br.com.cidadeemfoco.dto;

import br.com.cidadeemfoco.enums.CitizenRankingPeriod;

import java.time.Instant;
import java.util.List;

public record CitizenRankingResponse(
        CityResponse city,
        CitizenRankingPeriod period,
        Instant periodStart,
        Instant periodEnd,
        List<CitizenRankingEntryResponse> ranking
) {
}
