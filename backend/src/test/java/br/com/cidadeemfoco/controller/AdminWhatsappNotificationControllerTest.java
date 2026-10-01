package br.com.cidadeemfoco.controller;

import br.com.cidadeemfoco.dto.WhatsappNotificationResponse;
import br.com.cidadeemfoco.enums.AlertSeverity;
import br.com.cidadeemfoco.enums.ClimateAlertType;
import br.com.cidadeemfoco.enums.WhatsappNotificationStatus;
import br.com.cidadeemfoco.service.WhatsappNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
class AdminWhatsappNotificationControllerTest {

    @Mock
    private WhatsappNotificationService notificationService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AdminWhatsappNotificationController(notificationService))
                .build();
    }

    @Test
    void shouldListNotificationHistoryByStatus() throws Exception {
        when(notificationService.findAll(WhatsappNotificationStatus.PENDING))
                .thenReturn(List.of(response()));

        mockMvc.perform(get("/api/admin/whatsapp-notifications")
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[0].recipientPhoneMasked").value("**********9999"));

        verify(notificationService).findAll(WhatsappNotificationStatus.PENDING);
    }

    @Test
    void shouldFindNotificationById() throws Exception {
        when(notificationService.findById(30L)).thenReturn(response());

        mockMvc.perform(get("/api/admin/whatsapp-notifications/30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(30));
    }

    private WhatsappNotificationResponse response() {
        return new WhatsappNotificationResponse(
                30L,
                10L,
                20L,
                "**********9999",
                "Alerta de chuva",
                ClimateAlertType.CHUVA_INTENSA,
                AlertSeverity.ALTA,
                WhatsappNotificationStatus.PENDING,
                0,
                null,
                null,
                Instant.parse("2026-09-19T12:00:00Z"),
                Instant.parse("2026-09-19T12:00:00Z"),
                null
        );
    }
}
