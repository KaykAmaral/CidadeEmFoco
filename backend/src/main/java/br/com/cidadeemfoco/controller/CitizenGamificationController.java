package br.com.cidadeemfoco.controller;

import br.com.cidadeemfoco.dto.CitizenGamificationResponse;
import br.com.cidadeemfoco.dto.CitizenRankingResponse;
import br.com.cidadeemfoco.enums.CitizenRankingPeriod;
import br.com.cidadeemfoco.service.GamificationService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gamification")
public class CitizenGamificationController {

    private final GamificationService gamificationService;

    public CitizenGamificationController(GamificationService gamificationService) {
        this.gamificationService = gamificationService;
    }

    @GetMapping("/me")
    @Operation(summary = "Consultar XP e nivel do cidadao autenticado")
    public CitizenGamificationResponse findMine(Authentication authentication) {
        return gamificationService.findMine(authentication.getName());
    }

    @GetMapping("/ranking")
    @Operation(summary = "Consultar ranking de cidadaos da mesma cidade")
    public CitizenRankingResponse findRanking(
            Authentication authentication,
            @RequestParam(defaultValue = "MONTH") CitizenRankingPeriod period
    ) {
        return gamificationService.findCitizenRanking(authentication.getName(), period);
    }
}
