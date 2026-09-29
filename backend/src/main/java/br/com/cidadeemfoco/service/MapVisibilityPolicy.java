package br.com.cidadeemfoco.service;

import br.com.cidadeemfoco.entity.Occurrence;
import br.com.cidadeemfoco.enums.OccurrenceStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

public class MapVisibilityPolicy {
    private final Duration retention;
    private final Duration refreshInterval;
    private final Clock clock;

    public MapVisibilityPolicy(Duration retention, Duration refreshInterval, Clock clock) {
        if (retention.isNegative()) throw new IllegalArgumentException("O prazo do mapa nao pode ser negativo");
        if (refreshInterval.compareTo(Duration.ofSeconds(1)) < 0
                || refreshInterval.compareTo(Duration.ofHours(1)) > 0) {
            throw new IllegalArgumentException("A atualizacao do mapa deve estar entre 1 segundo e 1 hora");
        }
        this.retention = retention;
        this.refreshInterval = refreshInterval;
        this.clock = clock;
    }

    public Specification<Occurrence> visibleOccurrences() {
        Instant cutoff = clock.instant().minus(retention);
        return (root, query, builder) -> builder.or(
                builder.notEqual(root.get("status"), OccurrenceStatus.RESOLVIDA),
                builder.greaterThan(root.get("resolvedAt"), cutoff)
        );
    }

    public long refreshAfterMillis() {
        return refreshInterval.toMillis();
    }
}
