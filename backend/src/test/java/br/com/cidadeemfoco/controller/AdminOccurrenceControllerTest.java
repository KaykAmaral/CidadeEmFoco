package br.com.cidadeemfoco.controller;

import br.com.cidadeemfoco.dto.OccurrenceFilter;
import br.com.cidadeemfoco.dto.OccurrenceResponse;
import br.com.cidadeemfoco.enums.OccurrenceCategory;
import br.com.cidadeemfoco.enums.OccurrenceStatus;
import br.com.cidadeemfoco.enums.OccurrenceType;
import br.com.cidadeemfoco.enums.PerceivedRisk;
import br.com.cidadeemfoco.exception.GlobalExceptionHandler;
import br.com.cidadeemfoco.service.OccurrenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminOccurrenceControllerTest {

    @Mock
    private OccurrenceService occurrenceService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AdminOccurrenceController(occurrenceService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldUpdateStatus() throws Exception {
        when(occurrenceService.updateStatus(10L, OccurrenceStatus.EM_ANALISE))
                .thenReturn(response(OccurrenceStatus.EM_ANALISE));

        mockMvc.perform(patch("/api/admin/occurrences/10/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "EM_ANALISE"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_ANALISE"));
    }

    @Test
    void shouldRejectMissingStatus() throws Exception {
        mockMvc.perform(patch("/api/admin/occurrences/10/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.status").value("O status e obrigatorio"));
    }

    @Test
    void shouldForwardAdministrativeFilters() throws Exception {
        when(occurrenceService.findAll(any(OccurrenceFilter.class))).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/occurrences")
                        .param("category", "EVENTO_NATURAL")
                        .param("type", "ALAGAMENTO")
                        .param("status", "REGISTRADA")
                        .param("neighborhood", "Boqueirao"))
                .andExpect(status().isOk());

        verify(occurrenceService).findAll(new OccurrenceFilter(
                OccurrenceCategory.EVENTO_NATURAL,
                OccurrenceType.ALAGAMENTO,
                OccurrenceStatus.REGISTRADA,
                "Boqueirao"
        ));
    }

    @Test
    void shouldReturnAdministrativeOccurrenceDetails() throws Exception {
        when(occurrenceService.findById(10L)).thenReturn(response(OccurrenceStatus.REGISTRADA));

        mockMvc.perform(get("/api/admin/occurrences/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("REGISTRADA"));
    }

    private OccurrenceResponse response(OccurrenceStatus status) {
        return new OccurrenceResponse(
                10L,
                OccurrenceCategory.EVENTO_NATURAL,
                OccurrenceType.ALAGAMENTO,
                "Via alagada",
                PerceivedRisk.ALTO,
                new BigDecimal("-24.005000"),
                new BigDecimal("-46.402000"),
                "Boqueirao",
                null,
                null,
                status,
                Instant.parse("2026-08-21T18:00:00Z"),
                Instant.parse("2026-08-21T18:00:00Z")
        );
    }
}
