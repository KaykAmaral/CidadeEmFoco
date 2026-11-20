package br.com.cidadeemfoco.service;

import br.com.cidadeemfoco.dto.WhatsappNotificationResponse;
import br.com.cidadeemfoco.entity.ClimateAlert;
import br.com.cidadeemfoco.entity.User;
import br.com.cidadeemfoco.entity.WhatsappNotification;
import br.com.cidadeemfoco.enums.WhatsappNotificationStatus;
import br.com.cidadeemfoco.event.ClimateAlertActivatedEvent;
import br.com.cidadeemfoco.event.ClimateAlertUnavailableEvent;
import br.com.cidadeemfoco.exception.ResourceNotFoundException;
import br.com.cidadeemfoco.repository.ClimateAlertRepository;
import br.com.cidadeemfoco.repository.UserRepository;
import br.com.cidadeemfoco.repository.WhatsappNotificationRepository;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class WhatsappNotificationService {

    private static final Sort NEWEST_FIRST = Sort.by(Sort.Direction.DESC, "createdAt");
    private static final Set<WhatsappNotificationStatus> CANCELLABLE_STATUSES = Set.of(
            WhatsappNotificationStatus.PENDING,
            WhatsappNotificationStatus.FAILED
    );

    private final WhatsappNotificationRepository notificationRepository;
    private final ClimateAlertRepository climateAlertRepository;
    private final UserRepository userRepository;

    public WhatsappNotificationService(
            WhatsappNotificationRepository notificationRepository,
            ClimateAlertRepository climateAlertRepository,
            UserRepository userRepository
    ) {
        this.notificationRepository = notificationRepository;
        this.climateAlertRepository = climateAlertRepository;
        this.userRepository = userRepository;
    }

    public List<WhatsappNotificationResponse> findAll(String adminEmail, WhatsappNotificationStatus status) {
        Long cityId = findUser(adminEmail).getCity().getId();
        List<WhatsappNotification> notifications = status == null
                ? notificationRepository.findAllByUserCityId(cityId, NEWEST_FIRST)
                : notificationRepository.findByUserCityIdAndStatus(cityId, status, NEWEST_FIRST);
        return notifications.stream().map(WhatsappNotificationResponse::from).toList();
    }

    public WhatsappNotificationResponse findById(String adminEmail, Long id) {
        Long cityId = findUser(adminEmail).getCity().getId();
        return notificationRepository.findByIdAndUserCityId(id, cityId)
                .map(WhatsappNotificationResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Notificacao de WhatsApp nao encontrada"));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleActivatedAlert(ClimateAlertActivatedEvent event) {
        climateAlertRepository.findById(event.alertId())
                .filter(alert -> alert.isCurrentlyActive(Instant.now()))
                .ifPresent(this::enqueueForAlert);
    }

    @EventListener
    @Transactional
    public void handleUnavailableAlert(ClimateAlertUnavailableEvent event) {
        List<WhatsappNotification> notifications = notificationRepository
                .findByAlertIdInAndStatusIn(event.alertIds(), CANCELLABLE_STATUSES);
        notifications.forEach(WhatsappNotification::cancel);
        if (!notifications.isEmpty()) {
            notificationRepository.saveAll(notifications);
        }
    }

    @Scheduled(
            fixedDelayString = "${app.whatsapp.queue-sync-interval:1m}",
            initialDelayString = "${app.whatsapp.queue-sync-initial-delay:1m}"
    )
    @Transactional
    public int synchronizeQueue() {
        return climateAlertRepository.findCurrentlyActive(Instant.now())
                .stream()
                .mapToInt(this::enqueueForAlert)
                .sum();
    }

    private int enqueueForAlert(ClimateAlert alert) {
        List<User> recipients = userRepository
                .findByCityIdAndWhatsappNotificationsEnabledTrueAndWhatsappPhoneIsNotNull(
                        alert.getCity().getId()
                );
        List<WhatsappNotification> newNotifications = recipients.stream()
                .filter(user -> !notificationRepository.existsByAlertIdAndUserId(
                        alert.getId(), user.getId()
                ))
                .map(user -> new WhatsappNotification(alert, user))
                .toList();
        if (!newNotifications.isEmpty()) {
            notificationRepository.saveAll(newNotifications);
        }
        return newNotifications.size();
    }

    private User findUser(String email) {
        return userRepository.findByEmailIgnoreCase(email.trim())
                .filter(user -> user.getCity() != null)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));
    }
}
