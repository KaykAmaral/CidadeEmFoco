package br.com.cidadeemfoco.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.occurrences.auto-close.enabled", havingValue = "true")
public class OccurrenceAutoClosureScheduler {
    private static final Logger log = LoggerFactory.getLogger(OccurrenceAutoClosureScheduler.class);
    private final OccurrenceAutoClosureService service;

    public OccurrenceAutoClosureScheduler(OccurrenceAutoClosureService service) {
        this.service = service;
    }

    @Scheduled(fixedDelayString = "${app.occurrences.auto-close.check-interval}",
            initialDelayString = "${app.occurrences.auto-close.check-interval}")
    public void closeExpiredOccurrences() {
        int count = service.closeExpiredOccurrences();
        if (count > 0) log.info("Encerramento automatico: {} ocorrencias temporarias resolvidas", count);
    }
}
