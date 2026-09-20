package br.com.cidadeemfoco.service;

import br.com.cidadeemfoco.config.WhatsappCloudApiProperties;
import br.com.cidadeemfoco.entity.WhatsappNotification;
import br.com.cidadeemfoco.enums.WhatsappNotificationStatus;
import br.com.cidadeemfoco.repository.WhatsappNotificationRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@ConditionalOnProperty(name = "app.whatsapp.cloud-api.enabled", havingValue = "true")
public class WhatsappNotificationProcessor {

    private static final Set<WhatsappNotificationStatus> SENDABLE_STATUSES = Set.of(
            WhatsappNotificationStatus.PENDING,
            WhatsappNotificationStatus.FAILED
    );

    private final WhatsappNotificationRepository notificationRepository;
    private final WhatsappMessageSender messageSender;
    private final WhatsappCloudApiProperties properties;

    public WhatsappNotificationProcessor(
            WhatsappNotificationRepository notificationRepository,
            WhatsappMessageSender messageSender,
            WhatsappCloudApiProperties properties
    ) {
        this.notificationRepository = notificationRepository;
        this.messageSender = messageSender;
        this.properties = properties;
    }

    @Scheduled(
            fixedDelayString = "${app.whatsapp.cloud-api.process-interval:30s}",
            initialDelayString = "${app.whatsapp.cloud-api.process-initial-delay:30s}"
    )
    @Transactional
    public int processQueue() {
        List<WhatsappNotification> notifications = notificationRepository
                .findByStatusInAndAttemptCountLessThanOrderByCreatedAtAsc(
                        SENDABLE_STATUSES,
                        properties.maxAttempts(),
                        PageRequest.of(0, properties.batchSize())
                );

        notifications.forEach(this::send);
        notificationRepository.saveAll(notifications);
        return notifications.size();
    }

    private void send(WhatsappNotification notification) {
        notification.registerAttempt();
        try {
            String providerMessageId = messageSender.send(notification);
            notification.markAsSent(providerMessageId);
        } catch (RuntimeException exception) {
            notification.markAsFailed(readableError(exception));
        }
    }

    private String readableError(RuntimeException exception) {
        return exception.getMessage() == null || exception.getMessage().isBlank()
                ? "Falha inesperada ao enviar a notificacao"
                : exception.getMessage();
    }
}
