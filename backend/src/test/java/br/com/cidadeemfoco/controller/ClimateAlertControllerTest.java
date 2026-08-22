package br.com.cidadeemfoco.controller;

import br.com.cidadeemfoco.dto.ClimateAlertResponse;
import br.com.cidadeemfoco.dto.CreateClimateAlertRequest;
import br.com.cidadeemfoco.enums.AlertSeverity;
import br.com.cidadeemfoco.enums.ClimateAlertType;
import br.com.cidadeemfoco.exception.GlobalExceptionHandler;
import br.com.cidadeemfoco.service.ClimateAlertService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ClimateAlertControllerTest {

    @Mock
    private ClimateAlertService climateAlertService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(
                        new ClimateAlertController(climateAlertService),
                        new AdminClimateAlertController(climateAlertService)
                )
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldCreateAlertInactive() throws Exception {
        when(climateAlertService.create(any(CreateClimateAlertRequest.class)))
                .thenReturn(response(false));

        mockMvc.perform(post("/api/admin/alerts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validAlertJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.active").value(false))
                .andExpect(jsonPath("$.type").value("CHUVA_INTENSA"))
                .andExpect(jsonPath("$.disclaimer").exists());
    }

    @Test
    void shouldValidateRequiredAlertFields() throws Exception {
        mockMvc.perform(post("/api/admin/alerts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.title").exists())
                .andExpect(jsonPath("$.fieldErrors.type").exists())
                .andExpect(jsonPath("$.fieldErrors.severity").exists())
                .andExpect(jsonPath("$.fieldErrors.startAt").exists())
                .andExpect(jsonPath("$.fieldErrors.endAt").exists());
    }

    @Test
    void shouldListCurrentlyActiveAlerts() throws Exception {
        when(climateAlertService.findCurrentlyActive()).thenReturn(List.of(response(true)));

        mockMvc.perform(get("/api/alerts/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    void shouldActivateAndDeactivateAlert() throws Exception {
        when(climateAlertService.activate(10L)).thenReturn(response(true));
        when(climateAlertService.deactivate(10L)).thenReturn(response(false));

        mockMvc.perform(patch("/api/admin/alerts/10/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
        mockMvc.perform(patch("/api/admin/alerts/10/deactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        verify(climateAlertService).activate(10L);
        verify(climateAlertService).deactivate(10L);
    }

    @Test
    void shouldListAndFindAlertsForAdmin() throws Exception {
        when(climateAlertService.findAll()).thenReturn(List.of(response(false)));
        when(climateAlertService.findById(10L)).thenReturn(response(false));

        mockMvc.perform(get("/api/admin/alerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10));
        mockMvc.perform(get("/api/admin/alerts/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    private String validAlertJson() {
        return """
                {
                  "title": "Alerta de chuva intensa",
                  "type": "CHUVA_INTENSA",
                  "severity": "ALTA",
                  "description": "Possibilidade de chuva intensa em Praia Grande",
                  "startAt": "2026-08-21T12:00:00Z",
                  "endAt": "2026-08-21T18:00:00Z"
                }
                """;
    }

    private ClimateAlertResponse response(boolean active) {
        return new ClimateAlertResponse(
                10L,
                "Alerta de chuva intensa",
                ClimateAlertType.CHUVA_INTENSA,
                AlertSeverity.ALTA,
                "Possibilidade de chuva intensa em Praia Grande",
                Instant.parse("2026-08-21T12:00:00Z"),
                Instant.parse("2026-08-21T18:00:00Z"),
                active,
                Instant.parse("2026-08-21T10:00:00Z"),
                "Este alerta e demonstrativo e nao substitui informacoes oficiais."
        );
    }
}
