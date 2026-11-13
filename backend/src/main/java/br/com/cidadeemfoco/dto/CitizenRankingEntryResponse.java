package br.com.cidadeemfoco.dto;

public record CitizenRankingEntryResponse(
        int position,
        Long userId,
        String name,
        long xpEarnedInPeriod,
        long totalXp,
        int level
) {
}
