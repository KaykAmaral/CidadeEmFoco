package br.com.cidadeemfoco.service;

import br.com.cidadeemfoco.config.AutoClosureProperties;
import br.com.cidadeemfoco.entity.OccurrenceAutoClosure;
import br.com.cidadeemfoco.enums.OccurrenceStatus;
import br.com.cidadeemfoco.repository.OccurrenceAutoClosureRepository;
import br.com.cidadeemfoco.repository.OccurrenceRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.time.Instant;
import java.util.Set;

@Service
@ConditionalOnProperty(name = "app.occurrences.auto-close.enabled", havingValue = "true")
public class OccurrenceAutoClosureService {
    private static final Set<OccurrenceStatus> OPEN_STATUSES = Set.of(
            OccurrenceStatus.REGISTRADA, OccurrenceStatus.EM_ANALISE, OccurrenceStatus.EM_ATENDIMENTO);
    private final OccurrenceRepository occurrences;
    private final OccurrenceAutoClosureRepository audit;
    private final AutoClosureProperties settings;
    private final Clock clock;

    public OccurrenceAutoClosureService(OccurrenceRepository occurrences, OccurrenceAutoClosureRepository audit,
                                        AutoClosureProperties settings, Clock clock) {
        this.occurrences = occurrences;
        this.audit = audit;
        this.settings = settings;
        this.clock = clock;
    }

    @Transactional
    public int closeExpiredOccurrences() {
        if (settings.types().isEmpty()) return 0;
        Instant now = clock.instant();
        var candidates = occurrences.findEligibleForAutoClosure(settings.types(), OPEN_STATUSES,
                now.minus(settings.validity()), PageRequest.of(0, settings.batchSize()));
        int closed = 0;
        for (var occurrence : candidates) {
            var previousStatus = occurrence.getStatus();
            if (occurrence.resolveAutomatically(now)) {
                audit.save(new OccurrenceAutoClosure(occurrence, previousStatus, now, settings.validity().toSeconds()));
                closed++;
            }
        }
        occurrences.flush();
        return closed;
    }
}
