package br.com.cidadeemfoco.service;

import br.com.cidadeemfoco.config.WhatsappCloudApiProperties;
import br.com.cidadeemfoco.entity.ClimateAlert;
import br.com.cidadeemfoco.entity.User;
import br.com.cidadeemfoco.entity.WhatsappNotification;
import br.com.cidadeemfoco.enums.AlertSeverity;
import br.com.cidadeemfoco.enums.ClimateAlertType;
import br.com.cidadeemfoco.enums.UserRole;
import br.com.cidadeemfoco.enums.WhatsappNotificationStatus;
import br.com.cidadeemfoco.repository.WhatsappNotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WhatsappNotificationProcessorTest {

    @Mock
    private WhatsappNotificationRepository notificationRepository;

    @Mock
    private WhatsappMessageSender messageSender;

    private WhatsappNotificationProcessor processor;
    private WhatsappNotification notification;

    @BeforeEach
    void setUp() {
        WhatsappCloudApiProperties properties = new WhatsappCloudApiProperties(
                true, "https://graph.facebook.com", "v23.0", "123", "token",
                "cidade_em_foco_alerta_climatico", "pt_BR", "America/Sao_Paulo", 3, 20
        );
        processor = new WhatsappNotificationProcessor(
                notificationRepository, messageSender, properties
        );

        User user = new User("Ana", "ana@example.com", "hash", UserRole.CITIZEN);
        user.enableWhatsappNotifications("+5513999999999", Instant.now());
        ClimateAlert alert = new ClimateAlert(
                "Alerta de chuva", ClimateAlertType.CHUVA_INTENSA, AlertSeverity.ALTA,
                "Possibilidade de chuva intensa", Instant.now(), Instant.now().plusSeconds(3600)
        );
        notification = new WhatsappNotification(alert, user);
    }

    @Test
    void shouldSendPendingNotificationAndStoreProviderId() {
        when(notificationRepository.findByStatusInAndAttemptCountLessThanOrderByCreatedAtAsc(
                any(), eq(3), any(Pageable.class)
        )).thenReturn(List.of(notification));
        when(messageSender.send(notification)).thenReturn("wamid.123");

        int processed = processor.processQueue();

        assertThat(processed).isEqualTo(1);
        assertThat(notification.getStatus()).isEqualTo(WhatsappNotificationStatus.SENT);
        assertThat(notification.getAttemptCount()).isEqualTo(1);
        assertThat(notification.getProviderMessageId()).isEqualTo("wamid.123");
        verify(notificationRepository).saveAll(List.of(notification));
    }

    @Test
    void shouldMarkFailedAttemptWithoutInterruptingQueue() {
        when(notificationRepository.findByStatusInAndAttemptCountLessThanOrderByCreatedAtAsc(
                any(), eq(3), any(Pageable.class)
        )).thenReturn(List.of(notification));
        when(messageSender.send(notification)).thenThrow(new RuntimeException("Meta indisponivel"));

        processor.processQueue();

        assertThat(notification.getStatus()).isEqualTo(WhatsappNotificationStatus.FAILED);
        assertThat(notification.getAttemptCount()).isEqualTo(1);
        assertThat(notification.getLastError()).isEqualTo("Meta indisponivel");
    }
}
