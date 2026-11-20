package br.com.cidadeemfoco.controller;

import br.com.cidadeemfoco.dto.CityRankingEntryResponse;
import br.com.cidadeemfoco.dto.CityRankingResponse;
import br.com.cidadeemfoco.dto.CityResponse;
import br.com.cidadeemfoco.enums.RankingPeriod;
import br.com.cidadeemfoco.exception.GlobalExceptionHandler;
import br.com.cidadeemfoco.service.CityRankingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CityRankingControllerTest {

    @Mock
    private CityRankingService rankingService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new CityRankingController(rankingService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldReturnMonthlyRankingByDefault() throws Exception {
        when(rankingService.findRanking(RankingPeriod.MONTH)).thenReturn(response());

        mockMvc.perform(get("/api/rankings/cities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.period").value("MONTH"))
                .andExpect(jsonPath("$.ranking[0].position").value(1))
                .andExpect(jsonPath("$.ranking[0].city.name").value("Praia Grande"))
                .andExpect(jsonPath("$.ranking[0].resolutionRatePercent").value(75.00));

        verify(rankingService).findRanking(RankingPeriod.MONTH);
    }

    @Test
    void shouldRejectInvalidPeriod() throws Exception {
        mockMvc.perform(get("/api/rankings/cities").param("period", "DAY"))
                .andExpect(status().isBadRequest());
    }

    private CityRankingResponse response() {
        return new CityRankingResponse(
                RankingPeriod.MONTH,
                Instant.parse("2026-11-01T03:00:00Z"),
                Instant.parse("2026-12-01T03:00:00Z"),
                List.of(new CityRankingEntryResponse(
                        1,
                        new CityResponse(1L, "Praia Grande", "SP", "3541000"),
                        5,
                        4,
                        3,
                        1,
                        new BigDecimal("75.00")
                ))
        );
    }
}
