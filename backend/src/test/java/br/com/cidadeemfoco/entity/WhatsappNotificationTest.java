package br.com.cidadeemfoco.entity;

import br.com.cidadeemfoco.enums.AlertSeverity;
import br.com.cidadeemfoco.enums.ClimateAlertType;
import br.com.cidadeemfoco.enums.UserRole;
import br.com.cidadeemfoco.enums.WhatsappNotificationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WhatsappNotificationTest {

    private WhatsappNotification notification;

    @BeforeEach
    void setUp() {
        User user = new User("Ana", "ana@example.com", "hash", UserRole.CITIZEN);
        user.enableWhatsappNotifications("+5513999999999", Instant.now());
        ClimateAlert alert = new ClimateAlert(
                "Alerta de chuva",
                ClimateAlertType.CHUVA_INTENSA,
                AlertSeverity.ALTA,
                "Possibilidade de chuva intensa",
                Instant.parse("2026-09-19T12:00:00Z"),
                Instant.parse("2026-09-19T18:00:00Z")
        );
        notification = new WhatsappNotification(alert, user);
    }

    @Test
    void shouldCreatePendingNotificationWithSnapshots() {
        assertThat(notification.getStatus()).isEqualTo(WhatsappNotificationStatus.PENDING);
        assertThat(notification.getRecipientPhone()).isEqualTo("+5513999999999");
        assertThat(notification.getAlertTitle()).isEqualTo("Alerta de chuva");
        assertThat(notification.getAttemptCount()).isZero();
    }

    @Test
    void shouldTrackSuccessfulAttempt() {
        notification.registerAttempt();
        notification.markAsSent("wamid.123");

        assertThat(notification.getAttemptCount()).isEqualTo(1);
        assertThat(notification.getStatus()).isEqualTo(WhatsappNotificationStatus.SENT);
        assertThat(notification.getSentAt()).isNotNull();
        assertThat(notification.getLastError()).isNull();
        assertThat(notification.getProviderMessageId()).isEqualTo("wamid.123");
    }

    @Test
    void shouldTrackFailureAndAllowRetry() {
        notification.registerAttempt();
        notification.markAsFailed("Falha temporaria");
        notification.registerAttempt();

        assertThat(notification.getAttemptCount()).isEqualTo(2);
        assertThat(notification.getStatus()).isEqualTo(WhatsappNotificationStatus.FAILED);
        assertThat(notification.getLastError()).isNull();
    }

    @Test
    void shouldCancelPendingNotificationAndRejectAttempt() {
        notification.cancel();

        assertThat(notification.getStatus()).isEqualTo(WhatsappNotificationStatus.CANCELLED);
        assertThatThrownBy(notification::registerAttempt)
                .isInstanceOf(IllegalStateException.class);
    }
}
