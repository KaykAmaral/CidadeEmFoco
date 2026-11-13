package br.com.cidadeemfoco.dto;

public record CitizenGamificationResponse(
        long totalXp,
        int level,
        int xpInCurrentLevel,
        int xpForNextLevel
) {
}
