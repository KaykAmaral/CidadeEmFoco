package br.com.cidadeemfoco.dto;

import br.com.cidadeemfoco.entity.WhatsappNotification;
import br.com.cidadeemfoco.enums.AlertSeverity;
import br.com.cidadeemfoco.enums.ClimateAlertType;
import br.com.cidadeemfoco.enums.WhatsappNotificationStatus;

import java.time.Instant;

public record WhatsappNotificationResponse(
        Long id,
        Long alertId,
        Long userId,
        String recipientPhoneMasked,
        String alertTitle,
        ClimateAlertType alertType,
        AlertSeverity alertSeverity,
        WhatsappNotificationStatus status,
        int attemptCount,
        String lastError,
        String providerMessageId,
        Instant createdAt,
        Instant updatedAt,
        Instant sentAt
) {

    public static WhatsappNotificationResponse from(WhatsappNotification notification) {
        return new WhatsappNotificationResponse(
                notification.getId(),
                notification.getAlert() == null ? null : notification.getAlert().getId(),
                notification.getUser().getId(),
                maskPhone(notification.getRecipientPhone()),
                notification.getAlertTitle(),
                notification.getAlertType(),
                notification.getAlertSeverity(),
                notification.getStatus(),
                notification.getAttemptCount(),
                notification.getLastError(),
                notification.getProviderMessageId(),
                notification.getCreatedAt(),
                notification.getUpdatedAt(),
                notification.getSentAt()
        );
    }

    private static String maskPhone(String phone) {
        int visibleDigits = Math.min(4, phone.length());
        return "*".repeat(phone.length() - visibleDigits)
                + phone.substring(phone.length() - visibleDigits);
    }
}
