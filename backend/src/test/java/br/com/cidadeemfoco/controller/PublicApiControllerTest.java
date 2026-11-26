package br.com.cidadeemfoco.controller;

import br.com.cidadeemfoco.dto.PageResponse;
import br.com.cidadeemfoco.dto.PublicOccurrenceResponse;
import br.com.cidadeemfoco.enums.OccurrenceCategory;
import br.com.cidadeemfoco.enums.OccurrenceStatus;
import br.com.cidadeemfoco.enums.OccurrenceType;
import br.com.cidadeemfoco.enums.PerceivedRisk;
import br.com.cidadeemfoco.exception.GlobalExceptionHandler;
import br.com.cidadeemfoco.service.ClimateAlertService;
import br.com.cidadeemfoco.service.OccurrenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PublicApiControllerTest {

    @Mock
    private ClimateAlertService climateAlertService;
    @Mock
    private OccurrenceService occurrenceService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new PublicApiController(climateAlertService, occurrenceService)
                )
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldReturnPaginatedPublicOccurrencesWithoutPersonalOrPreciseData() throws Exception {
        PublicOccurrenceResponse occurrence = new PublicOccurrenceResponse(
                42L, OccurrenceCategory.EVENTO_NATURAL, OccurrenceType.ALAGAMENTO,
                PerceivedRisk.ALTO, "Boqueirao", OccurrenceStatus.EM_ATENDIMENTO,
                Instant.parse("2026-10-01T12:00:00Z"), null, 3
        );
        when(occurrenceService.findPublic(any(Pageable.class))).thenReturn(
                new PageResponse<>(List.of(occurrence), 0, 20, 1, 1, true, true)
        );

        mockMvc.perform(get("/api/v1/public/occurrences"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].neighborhood").value("Boqueirao"))
                .andExpect(jsonPath("$.content[0].reports").value(3))
                .andExpect(jsonPath("$.content[0].description").doesNotExist())
                .andExpect(jsonPath("$.content[0].address").doesNotExist())
                .andExpect(jsonPath("$.content[0].latitude").doesNotExist())
                .andExpect(jsonPath("$.content[0].email").doesNotExist())
                .andExpect(jsonPath("$.content[0].phoneNumber").doesNotExist());
    }

    @Test
    void shouldRejectInvalidPagination() throws Exception {
        mockMvc.perform(get("/api/v1/public/occurrences?page=-1&size=101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Requisicao invalida"));
    }
}
