package br.com.cidadeemfoco.controller;

import br.com.cidadeemfoco.dto.CitizenGamificationResponse;
import br.com.cidadeemfoco.dto.CitizenRankingEntryResponse;
import br.com.cidadeemfoco.dto.CitizenRankingResponse;
import br.com.cidadeemfoco.dto.CityResponse;
import br.com.cidadeemfoco.enums.CitizenRankingPeriod;
import br.com.cidadeemfoco.exception.GlobalExceptionHandler;
import br.com.cidadeemfoco.service.GamificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CitizenGamificationControllerTest {

    @Mock
    private GamificationService gamificationService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new CitizenGamificationController(gamificationService))
                .defaultRequest(get("/").principal(UsernamePasswordAuthenticationToken.authenticated(
                        "ana@example.com", null, List.of()
                )))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldReturnAuthenticatedCitizenProgress() throws Exception {
        when(gamificationService.findMine("ana@example.com"))
                .thenReturn(new CitizenGamificationResponse(235, 3, 35, 65));

        mockMvc.perform(get("/api/gamification/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalXp").value(235))
                .andExpect(jsonPath("$.level").value(3))
                .andExpect(jsonPath("$.xpForNextLevel").value(65));
    }

    @Test
    void shouldReturnYearlyCityRanking() throws Exception {
        when(gamificationService.findCitizenRanking("ana@example.com", CitizenRankingPeriod.YEAR))
                .thenReturn(ranking());

        mockMvc.perform(get("/api/gamification/ranking").param("period", "YEAR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city.name").value("Praia Grande"))
                .andExpect(jsonPath("$.ranking[0].name").value("Ana"));

        verify(gamificationService).findCitizenRanking("ana@example.com", CitizenRankingPeriod.YEAR);
    }

    private CitizenRankingResponse ranking() {
        return new CitizenRankingResponse(
                new CityResponse(1L, "Praia Grande", "SP", "3541000"),
                CitizenRankingPeriod.YEAR,
                Instant.parse("2026-01-01T03:00:00Z"),
                Instant.parse("2027-01-01T03:00:00Z"),
                List.of(new CitizenRankingEntryResponse(1, 10L, "Ana", 50, 250, 3))
        );
    }
}
