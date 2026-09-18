package br.com.cidadeemfoco.service;

import br.com.cidadeemfoco.entity.Occurrence;
import br.com.cidadeemfoco.enums.OccurrenceCategory;
import br.com.cidadeemfoco.enums.OccurrenceStatus;
import br.com.cidadeemfoco.enums.OccurrenceType;
import br.com.cidadeemfoco.repository.OccurrenceRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

@Service
public class OccurrenceAutoResolutionService {

    private static final Set<OccurrenceType> TEMPORARY_TYPES = Set.of(
            OccurrenceType.ALAGAMENTO,
            OccurrenceType.ENCHENTE,
            OccurrenceType.VENTOS_FORTES,
            OccurrenceType.RESSACA_MARITIMA,
            OccurrenceType.CHUVA_INTENSA
    );

    private static final Set<OccurrenceStatus> OPEN_STATUSES = Set.of(
            OccurrenceStatus.REGISTRADA,
            OccurrenceStatus.EM_ANALISE,
            OccurrenceStatus.EM_ATENDIMENTO
    );

    private final OccurrenceRepository occurrenceRepository;
    private final Duration temporaryEventLifetime;

    public OccurrenceAutoResolutionService(
            OccurrenceRepository occurrenceRepository,
            @Value("${app.occurrences.temporary-event-lifetime:6h}") Duration temporaryEventLifetime
    ) {
        if (temporaryEventLifetime.isZero() || temporaryEventLifetime.isNegative()) {
            throw new IllegalArgumentException("O tempo de vida dos eventos temporarios deve ser positivo");
        }
        this.occurrenceRepository = occurrenceRepository;
        this.temporaryEventLifetime = temporaryEventLifetime;
    }

    @Scheduled(
            fixedDelayString = "${app.occurrences.auto-resolution-interval:5m}",
            initialDelayString = "${app.occurrences.auto-resolution-initial-delay:5m}"
    )
    @Transactional
    public int resolveExpiredTemporaryOccurrences() {
        Instant createdBefore = Instant.now().minus(temporaryEventLifetime);
        var expiredOccurrences = occurrenceRepository
                .findByCategoryAndTypeInAndStatusInAndCreatedAtLessThanEqual(
                        OccurrenceCategory.EVENTO_NATURAL,
                        TEMPORARY_TYPES,
                        OPEN_STATUSES,
                        createdBefore
                );

        expiredOccurrences.forEach(occurrence -> occurrence.changeStatus(OccurrenceStatus.RESOLVIDA));
        if (!expiredOccurrences.isEmpty()) {
            occurrenceRepository.saveAll(expiredOccurrences);
        }

        return expiredOccurrences.size();
    }
}
