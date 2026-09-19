package br.com.cidadeemfoco.service;

import br.com.cidadeemfoco.dto.ClimateAlertResponse;
import br.com.cidadeemfoco.dto.CreateClimateAlertRequest;
import br.com.cidadeemfoco.entity.ClimateAlert;
import br.com.cidadeemfoco.enums.AlertSeverity;
import br.com.cidadeemfoco.enums.ClimateAlertType;
import br.com.cidadeemfoco.event.ClimateAlertsChangedEvent;
import br.com.cidadeemfoco.exception.BusinessRuleException;
import br.com.cidadeemfoco.exception.ResourceNotFoundException;
import br.com.cidadeemfoco.repository.ClimateAlertRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClimateAlertServiceTest {

    @Mock
    private ClimateAlertRepository climateAlertRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private ClimateAlertService climateAlertService;

    @BeforeEach
    void setUp() {
        climateAlertService = new ClimateAlertService(climateAlertRepository, eventPublisher);
    }

    @Test
    void shouldCreateAlertInactive() {
        when(climateAlertRepository.save(any(ClimateAlert.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ClimateAlertResponse response = climateAlertService.create(validRequest());

        assertThat(response.active()).isFalse();
        assertThat(response.type()).isEqualTo(ClimateAlertType.CHUVA_INTENSA);
        assertThat(response.severity()).isEqualTo(AlertSeverity.ALTA);
        assertThat(response.disclaimer()).contains("nao substitui informacoes oficiais");
        verify(eventPublisher).publishEvent(any(ClimateAlertsChangedEvent.class));
    }

    @Test
    void shouldRejectInvalidPeriod() {
        CreateClimateAlertRequest request = new CreateClimateAlertRequest(
                "Periodo invalido",
                ClimateAlertType.OUTRO,
                AlertSeverity.BAIXA,
                "Alerta demonstrativo",
                Instant.parse("2026-08-21T18:00:00Z"),
                Instant.parse("2026-08-21T12:00:00Z")
        );

        assertThatThrownBy(() -> climateAlertService.create(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("posterior");
        verify(climateAlertRepository, never()).save(any());
    }

    @Test
    void shouldReturnOnlyCurrentlyActiveAlerts() {
        ClimateAlert alert = alert();
        alert.activate();
        when(climateAlertRepository.findCurrentlyActive(any(Instant.class)))
                .thenReturn(List.of(alert));

        List<ClimateAlertResponse> responses = climateAlertService.findCurrentlyActive();

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().active()).isTrue();
        verify(climateAlertRepository).findCurrentlyActive(any(Instant.class));
    }

    @Test
    void shouldListAllAlertsNewestFirstForAdmin() {
        when(climateAlertRepository.findAll(any(Sort.class))).thenReturn(List.of(alert()));

        List<ClimateAlertResponse> responses = climateAlertService.findAll();

        assertThat(responses).hasSize(1);
        verify(climateAlertRepository).findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    @Test
    void shouldActivateAndDeactivateAlert() {
        ClimateAlert alert = alert();
        when(climateAlertRepository.findById(10L)).thenReturn(Optional.of(alert));
        when(climateAlertRepository.saveAndFlush(alert)).thenReturn(alert);

        ClimateAlertResponse activated = climateAlertService.activate(10L);
        ClimateAlertResponse deactivated = climateAlertService.deactivate(10L);

        assertThat(activated.active()).isTrue();
        assertThat(deactivated.active()).isFalse();
        verify(climateAlertRepository, org.mockito.Mockito.times(2)).saveAndFlush(alert);
        verify(eventPublisher, org.mockito.Mockito.times(2))
                .publishEvent(any(ClimateAlertsChangedEvent.class));
    }

    @Test
    void shouldReturnNotFoundForMissingAlert() {
        when(climateAlertRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> climateAlertService.activate(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Alerta climatico nao encontrado");
    }

    @Test
    void shouldDeleteAnyAlertAndPublishUpdate() {
        ClimateAlert alert = alert();
        when(climateAlertRepository.findById(10L)).thenReturn(Optional.of(alert));

        climateAlertService.delete(10L);

        verify(climateAlertRepository).delete(alert);
        verify(eventPublisher).publishEvent(any(ClimateAlertsChangedEvent.class));
    }

    private CreateClimateAlertRequest validRequest() {
        return new CreateClimateAlertRequest(
                "Alerta de chuva intensa",
                ClimateAlertType.CHUVA_INTENSA,
                AlertSeverity.ALTA,
                "Possibilidade de chuva intensa em Praia Grande",
                Instant.parse("2026-08-21T12:00:00Z"),
                Instant.parse("2026-08-21T18:00:00Z")
        );
    }

    private ClimateAlert alert() {
        CreateClimateAlertRequest request = validRequest();
        return new ClimateAlert(
                request.title(),
                request.type(),
                request.severity(),
                request.description(),
                request.startAt(),
                request.endAt()
        );
    }
}
