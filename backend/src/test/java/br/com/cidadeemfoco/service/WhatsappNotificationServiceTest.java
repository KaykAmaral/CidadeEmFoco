package br.com.cidadeemfoco.service;

import br.com.cidadeemfoco.entity.ClimateAlert;
import br.com.cidadeemfoco.entity.User;
import br.com.cidadeemfoco.entity.WhatsappNotification;
import br.com.cidadeemfoco.enums.AlertSeverity;
import br.com.cidadeemfoco.enums.ClimateAlertType;
import br.com.cidadeemfoco.enums.UserRole;
import br.com.cidadeemfoco.enums.WhatsappNotificationStatus;
import br.com.cidadeemfoco.event.ClimateAlertActivatedEvent;
import br.com.cidadeemfoco.event.ClimateAlertUnavailableEvent;
import br.com.cidadeemfoco.repository.ClimateAlertRepository;
import br.com.cidadeemfoco.repository.UserRepository;
import br.com.cidadeemfoco.repository.WhatsappNotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WhatsappNotificationServiceTest {

    @Mock
    private WhatsappNotificationRepository notificationRepository;

    @Mock
    private ClimateAlertRepository climateAlertRepository;

    @Mock
    private UserRepository userRepository;

    private WhatsappNotificationService service;
    private ClimateAlert alert;
    private User citizen;

    @BeforeEach
    void setUp() {
        service = new WhatsappNotificationService(
                notificationRepository,
                climateAlertRepository,
                userRepository
        );
        Instant now = Instant.now();
        alert = new ClimateAlert(
                "Alerta de chuva",
                ClimateAlertType.CHUVA_INTENSA,
                AlertSeverity.ALTA,
                "Possibilidade de chuva intensa",
                now.minusSeconds(60),
                now.plusSeconds(3600)
        );
        alert.activate();
        ReflectionTestUtils.setField(alert, "id", 10L);

        citizen = new User("Ana", "ana@example.com", "hash", UserRole.CITIZEN);
        citizen.enableWhatsappNotifications("+5513999999999", now);
        ReflectionTestUtils.setField(citizen, "id", 20L);
    }

    @Test
    void shouldEnqueueSubscribedUsersWhenAlertIsActivated() {
        when(climateAlertRepository.findById(10L)).thenReturn(Optional.of(alert));
        when(userRepository.findByWhatsappNotificationsEnabledTrueAndWhatsappPhoneIsNotNull())
                .thenReturn(List.of(citizen));
        when(notificationRepository.existsByAlertIdAndUserId(10L, 20L)).thenReturn(false);

        service.handleActivatedAlert(new ClimateAlertActivatedEvent(10L));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<WhatsappNotification>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(1);
        assertThat(captor.getValue().getFirst().getStatus())
                .isEqualTo(WhatsappNotificationStatus.PENDING);
    }

    @Test
    void shouldNotCreateDuplicateNotification() {
        when(climateAlertRepository.findById(10L)).thenReturn(Optional.of(alert));
        when(userRepository.findByWhatsappNotificationsEnabledTrueAndWhatsappPhoneIsNotNull())
                .thenReturn(List.of(citizen));
        when(notificationRepository.existsByAlertIdAndUserId(10L, 20L)).thenReturn(true);

        service.handleActivatedAlert(new ClimateAlertActivatedEvent(10L));

        verify(notificationRepository, never()).saveAll(any());
    }

    @Test
    void shouldSynchronizeAlertsThatBecameActiveLater() {
        when(climateAlertRepository.findCurrentlyActive(any(Instant.class))).thenReturn(List.of(alert));
        when(userRepository.findByWhatsappNotificationsEnabledTrueAndWhatsappPhoneIsNotNull())
                .thenReturn(List.of(citizen));
        when(notificationRepository.existsByAlertIdAndUserId(10L, 20L)).thenReturn(false);

        int created = service.synchronizeQueue();

        assertThat(created).isEqualTo(1);
        verify(notificationRepository).saveAll(any());
    }

    @Test
    void shouldCancelPendingAndFailedNotificationsForUnavailableAlert() {
        WhatsappNotification pending = new WhatsappNotification(alert, citizen);
        WhatsappNotification failed = new WhatsappNotification(alert, citizen);
        failed.registerAttempt();
        failed.markAsFailed("Falha");
        when(notificationRepository.findByAlertIdInAndStatusIn(
                eq(List.of(10L)),
                eq(Set.of(WhatsappNotificationStatus.PENDING, WhatsappNotificationStatus.FAILED))
        )).thenReturn(List.of(pending, failed));

        service.handleUnavailableAlert(new ClimateAlertUnavailableEvent(10L));

        assertThat(pending.getStatus()).isEqualTo(WhatsappNotificationStatus.CANCELLED);
        assertThat(failed.getStatus()).isEqualTo(WhatsappNotificationStatus.CANCELLED);
        verify(notificationRepository).saveAll(List.of(pending, failed));
    }
}
