package br.com.cidadeemfoco.entity;

import br.com.cidadeemfoco.enums.CitizenXpEventType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(
        name = "citizen_xp_events",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_citizen_xp_event",
                columnNames = {"user_id", "occurrence_id", "event_type"}
        )
)
public class CitizenXpEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "occurrence_id", nullable = false)
    private Occurrence occurrence;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "event_type", nullable = false, length = 30)
    private CitizenXpEventType eventType;

    @Column(nullable = false)
    private int points;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected CitizenXpEvent() {
    }

    public CitizenXpEvent(
            User user,
            Occurrence occurrence,
            CitizenXpEventType eventType,
            int points
    ) {
        this.user = Objects.requireNonNull(user);
        this.occurrence = Objects.requireNonNull(occurrence);
        this.eventType = Objects.requireNonNull(eventType);
        if (points <= 0) {
            throw new IllegalArgumentException("Os pontos de XP devem ser positivos");
        }
        this.points = points;
    }

    @PrePersist
    private void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Occurrence getOccurrence() { return occurrence; }
    public CitizenXpEventType getEventType() { return eventType; }
    public int getPoints() { return points; }
    public Instant getCreatedAt() { return createdAt; }
}
