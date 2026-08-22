package br.com.cidadeemfoco.entity;

import br.com.cidadeemfoco.enums.AlertSeverity;
import br.com.cidadeemfoco.enums.ClimateAlertType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "climate_alerts")
public class ClimateAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "alert_type", nullable = false, length = 40)
    private ClimateAlertType type;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private AlertSeverity severity;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(name = "start_at", nullable = false)
    private Instant startAt;

    @Column(name = "end_at", nullable = false)
    private Instant endAt;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected ClimateAlert() {
    }

    public ClimateAlert(
            String title,
            ClimateAlertType type,
            AlertSeverity severity,
            String description,
            Instant startAt,
            Instant endAt
    ) {
        this.title = Objects.requireNonNull(title);
        this.type = Objects.requireNonNull(type);
        this.severity = Objects.requireNonNull(severity);
        this.description = Objects.requireNonNull(description);
        this.startAt = Objects.requireNonNull(startAt);
        this.endAt = Objects.requireNonNull(endAt);
        if (!endAt.isAfter(startAt)) {
            throw new IllegalArgumentException("O fim do alerta deve ser posterior ao inicio");
        }
    }

    @PrePersist
    private void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public void activate() {
        active = true;
    }

    public void deactivate() {
        active = false;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public ClimateAlertType getType() {
        return type;
    }

    public AlertSeverity getSeverity() {
        return severity;
    }

    public String getDescription() {
        return description;
    }

    public Instant getStartAt() {
        return startAt;
    }

    public Instant getEndAt() {
        return endAt;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
