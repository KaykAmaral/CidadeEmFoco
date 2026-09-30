package br.com.cidadeemfoco.entity;

import br.com.cidadeemfoco.enums.OccurrenceStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;

@Entity
@Table(name = "occurrence_auto_closures")
public class OccurrenceAutoClosure {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "occurrence_id", nullable = false, unique = true)
    private Occurrence occurrence;

    @Enumerated(EnumType.STRING) @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "previous_status", nullable = false, length = 30)
    private OccurrenceStatus previousStatus;

    @Column(name = "closed_at", nullable = false)
    private Instant closedAt;

    @Column(name = "validity_seconds", nullable = false)
    private long validitySeconds;

    protected OccurrenceAutoClosure() {}

    public OccurrenceAutoClosure(Occurrence occurrence, OccurrenceStatus previousStatus,
                                 Instant closedAt, long validitySeconds) {
        this.occurrence = occurrence;
        this.previousStatus = previousStatus;
        this.closedAt = closedAt;
        this.validitySeconds = validitySeconds;
    }

    public Instant getClosedAt() { return closedAt; }
    public OccurrenceStatus getPreviousStatus() { return previousStatus; }
    public long getValiditySeconds() { return validitySeconds; }
}
