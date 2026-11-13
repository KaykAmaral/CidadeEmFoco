package br.com.cidadeemfoco.dto;

import br.com.cidadeemfoco.enums.RankingPeriod;

import java.time.Instant;
import java.util.List;

public record CityRankingResponse(
        RankingPeriod period,
        Instant periodStart,
        Instant periodEnd,
        List<CityRankingEntryResponse> ranking
) {
}
