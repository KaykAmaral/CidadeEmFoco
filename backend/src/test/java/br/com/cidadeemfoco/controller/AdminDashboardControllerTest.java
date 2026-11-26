package br.com.cidadeemfoco.controller;

import br.com.cidadeemfoco.dto.AdminOccurrenceMetricsResponse;
import br.com.cidadeemfoco.dto.CityResponse;
import br.com.cidadeemfoco.dto.OccurrenceFilter;
import br.com.cidadeemfoco.enums.OccurrenceCategory;
import br.com.cidadeemfoco.enums.OccurrenceStatus;
import br.com.cidadeemfoco.enums.OccurrenceType;
import br.com.cidadeemfoco.exception.GlobalExceptionHandler;
import br.com.cidadeemfoco.service.AdminDashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminDashboardControllerTest {

    @Mock
    private AdminDashboardService dashboardService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AdminDashboardController(dashboardService))
                .defaultRequest(get("/").principal(
                        UsernamePasswordAuthenticationToken.authenticated(
                                "admin@example.com", null, List.of()
                        )
                ))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldReturnFilteredOccurrenceMetrics() throws Exception {
        Instant from = Instant.parse("2026-10-01T00:00:00Z");
        Instant to = Instant.parse("2026-10-31T23:59:59Z");
        OccurrenceFilter filter = new OccurrenceFilter(
                OccurrenceCategory.EVENTO_NATURAL,
                OccurrenceType.ALAGAMENTO,
                OccurrenceStatus.RESOLVIDA,
                "Boqueirao",
                from,
                to
        );
        when(dashboardService.occurrenceMetrics("admin@example.com", filter)).thenReturn(response(from, to));

        mockMvc.perform(get("/api/admin/dashboard/occurrences")
                        .param("category", "EVENTO_NATURAL")
                        .param("type", "ALAGAMENTO")
                        .param("status", "RESOLVIDA")
                        .param("neighborhood", "Boqueirao")
                        .param("createdFrom", from.toString())
                        .param("createdTo", to.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city.ibgeCode").value("3541000"))
                .andExpect(jsonPath("$.totalCases").value(4))
                .andExpect(jsonPath("$.resolutionRatePercent").value(66.67));

        verify(dashboardService).occurrenceMetrics("admin@example.com", filter);
    }

    private AdminOccurrenceMetricsResponse response(Instant from, Instant to) {
        return new AdminOccurrenceMetricsResponse(
                new CityResponse(1L, "Praia Grande", "SP", "3541000"),
                from,
                to,
                4,
                7,
                1,
                2,
                1,
                1,
                new BigDecimal("66.67"),
                90L,
                Map.of(OccurrenceStatus.RESOLVIDA, 2L),
                Map.of(OccurrenceCategory.EVENTO_NATURAL, 4L),
                Map.of(OccurrenceType.ALAGAMENTO, 4L),
                List.of(),
                List.of()
        );
    }
}
