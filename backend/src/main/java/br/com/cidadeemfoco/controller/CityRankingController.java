package br.com.cidadeemfoco.controller;

import br.com.cidadeemfoco.dto.CityRankingResponse;
import br.com.cidadeemfoco.enums.RankingPeriod;
import br.com.cidadeemfoco.service.CityRankingService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rankings")
public class CityRankingController {

    private final CityRankingService rankingService;

    public CityRankingController(CityRankingService rankingService) {
        this.rankingService = rankingService;
    }

    @GetMapping("/cities")
    @Operation(summary = "Consultar ranking de resolucao das prefeituras")
    public CityRankingResponse findCities(
            @RequestParam(defaultValue = "MONTH") RankingPeriod period
    ) {
        return rankingService.findRanking(period);
    }
}
