package br.com.cidadeemfoco.controller;

import br.com.cidadeemfoco.dto.CreateOccurrenceRequest;
import br.com.cidadeemfoco.dto.OccurrenceFilter;
import br.com.cidadeemfoco.dto.OccurrenceResponse;
import br.com.cidadeemfoco.enums.OccurrenceCategory;
import br.com.cidadeemfoco.enums.OccurrenceStatus;
import br.com.cidadeemfoco.enums.OccurrenceType;
import br.com.cidadeemfoco.enums.PerceivedRisk;
import br.com.cidadeemfoco.exception.GlobalExceptionHandler;
import br.com.cidadeemfoco.exception.ResourceNotFoundException;
import br.com.cidadeemfoco.service.OccurrenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OccurrenceControllerTest {

    @Mock
    private OccurrenceService occurrenceService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new OccurrenceController(occurrenceService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldCreateOccurrence() throws Exception {
        when(occurrenceService.create(eq("citizen@example.com"), any(CreateOccurrenceRequest.class)))
                .thenReturn(response());

        mockMvc.perform(post("/api/occurrences")
                        .principal(authentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "category": "EVENTO_NATURAL",
                                  "type": "ALAGAMENTO",
                                  "description": "Via alagada",
                                  "perceivedRisk": "ALTO",
                                  "latitude": -24.005000,
                                  "longitude": -46.402000,
                                  "neighborhood": "Boqueirao",
                                  "address": "Avenida Presidente Costa e Silva"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(15))
                .andExpect(jsonPath("$.status").value("REGISTRADA"));
    }

    @Test
    void shouldReturnValidationErrors() throws Exception {
        mockMvc.perform(post("/api/occurrences")
                        .principal(authentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "category": "EVENTO_NATURAL",
                                  "type": "ALAGAMENTO",
                                  "description": "",
                                  "perceivedRisk": "ALTO",
                                  "latitude": -91,
                                  "longitude": -46.402000
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Existem campos invalidos"))
                .andExpect(jsonPath("$.fieldErrors.description").exists())
                .andExpect(jsonPath("$.fieldErrors.latitude").exists());
    }

    @Test
    void shouldForwardFiltersToService() throws Exception {
        when(occurrenceService.findAll(any(OccurrenceFilter.class))).thenReturn(List.of());

        mockMvc.perform(get("/api/occurrences")
                        .param("category", "INFRAESTRUTURA_URBANA")
                        .param("type", "BURACO_RUA")
                        .param("status", "REGISTRADA")
                        .param("neighborhood", "Boqueirao"))
                .andExpect(status().isOk());

        verify(occurrenceService).findAll(new OccurrenceFilter(
                OccurrenceCategory.INFRAESTRUTURA_URBANA,
                OccurrenceType.BURACO_RUA,
                OccurrenceStatus.REGISTRADA,
                "Boqueirao",
                null,
                null
        ));
    }

    @Test
    void shouldReturnStructuredNotFoundError() throws Exception {
        when(occurrenceService.findById(99L))
                .thenThrow(new ResourceNotFoundException("Ocorrencia nao encontrada"));

        mockMvc.perform(get("/api/occurrences/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Ocorrencia nao encontrada"))
                .andExpect(jsonPath("$.path").value("/api/occurrences/99"));
    }

    @Test
    void shouldListOccurrencesFromAuthenticatedUser() throws Exception {
        when(occurrenceService.findByUser("citizen@example.com")).thenReturn(List.of(response()));

        mockMvc.perform(get("/api/occurrences/mine")
                        .principal(authentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(15));
    }

    @Test
    void shouldListOccurrencesVisibleOnMap() throws Exception {
        when(occurrenceService.findVisibleOnMap()).thenReturn(List.of(response()));

        mockMvc.perform(get("/api/occurrences/map"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(15));

        verify(occurrenceService).findVisibleOnMap();
    }

    private OccurrenceResponse response() {
        return new OccurrenceResponse(
                15L,
                OccurrenceCategory.EVENTO_NATURAL,
                OccurrenceType.ALAGAMENTO,
                "Via alagada",
                PerceivedRisk.ALTO,
                new BigDecimal("-24.005000"),
                new BigDecimal("-46.402000"),
                "Boqueirao",
                "Avenida Presidente Costa e Silva",
                null,
                OccurrenceStatus.REGISTRADA,
                Instant.parse("2026-08-21T18:00:00Z"),
                Instant.parse("2026-08-21T18:00:00Z")
        );
    }

    private Authentication authentication() {
        return UsernamePasswordAuthenticationToken.authenticated(
                "citizen@example.com",
                null,
                List.of()
        );
    }
}
