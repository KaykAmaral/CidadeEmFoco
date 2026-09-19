package br.com.cidadeemfoco.service;

import br.com.cidadeemfoco.entity.ClimateAlert;
import br.com.cidadeemfoco.enums.AlertSeverity;
import br.com.cidadeemfoco.enums.ClimateAlertType;
import br.com.cidadeemfoco.event.ClimateAlertsChangedEvent;
import br.com.cidadeemfoco.repository.ClimateAlertRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClimateAlertCleanupServiceTest {

    @Mock
    private ClimateAlertRepository climateAlertRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private ClimateAlertCleanupService cleanupService;

    @BeforeEach
    void setUp() {
        cleanupService = new ClimateAlertCleanupService(climateAlertRepository, eventPublisher);
    }

    @Test
    void shouldDeleteExpiredAlertsAndPublishUpdate() {
        ClimateAlert expiredAlert = expiredAlert();
        when(climateAlertRepository.findByEndAtLessThanEqual(any(Instant.class)))
                .thenReturn(List.of(expiredAlert));

        int deleted = cleanupService.deleteExpiredAlerts();

        assertThat(deleted).isEqualTo(1);
        verify(climateAlertRepository).deleteAll(List.of(expiredAlert));
        verify(eventPublisher).publishEvent(any(ClimateAlertsChangedEvent.class));
    }

    @Test
    void shouldDoNothingWhenThereAreNoExpiredAlerts() {
        when(climateAlertRepository.findByEndAtLessThanEqual(any(Instant.class)))
                .thenReturn(List.of());

        int deleted = cleanupService.deleteExpiredAlerts();

        assertThat(deleted).isZero();
        verify(climateAlertRepository, never()).deleteAll(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    private ClimateAlert expiredAlert() {
        return new ClimateAlert(
                "Alerta expirado",
                ClimateAlertType.CHUVA_INTENSA,
                AlertSeverity.MODERADA,
                "Alerta com validade encerrada",
                Instant.parse("2026-08-20T10:00:00Z"),
                Instant.parse("2026-08-20T12:00:00Z")
        );
    }
}
