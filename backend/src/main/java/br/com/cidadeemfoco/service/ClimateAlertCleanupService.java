package br.com.cidadeemfoco.service;

import br.com.cidadeemfoco.entity.ClimateAlert;
import br.com.cidadeemfoco.event.ClimateAlertsChangedEvent;
import br.com.cidadeemfoco.event.ClimateAlertUnavailableEvent;
import br.com.cidadeemfoco.repository.ClimateAlertRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class ClimateAlertCleanupService {

    private final ClimateAlertRepository climateAlertRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ClimateAlertCleanupService(
            ClimateAlertRepository climateAlertRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.climateAlertRepository = climateAlertRepository;
        this.eventPublisher = eventPublisher;
    }

    @Scheduled(
            fixedDelayString = "${app.alerts.cleanup-interval:5m}",
            initialDelayString = "${app.alerts.cleanup-initial-delay:5m}"
    )
    @Transactional
    public int deleteExpiredAlerts() {
        List<ClimateAlert> expiredAlerts = climateAlertRepository.findByEndAtLessThanEqual(Instant.now());
        if (expiredAlerts.isEmpty()) {
            return 0;
        }

        eventPublisher.publishEvent(new ClimateAlertUnavailableEvent(
                expiredAlerts.stream().map(ClimateAlert::getId).toList()
        ));
        climateAlertRepository.deleteAll(expiredAlerts);
        eventPublisher.publishEvent(new ClimateAlertsChangedEvent());
        return expiredAlerts.size();
    }
}
