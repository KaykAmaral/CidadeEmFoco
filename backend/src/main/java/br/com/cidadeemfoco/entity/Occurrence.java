package br.com.cidadeemfoco.entity;

import br.com.cidadeemfoco.enums.OccurrenceCategory;
import br.com.cidadeemfoco.enums.OccurrenceStatus;
import br.com.cidadeemfoco.enums.OccurrenceType;
import br.com.cidadeemfoco.enums.PerceivedRisk;
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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "occurrences")
public class Occurrence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 40)
    private OccurrenceCategory category;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "occurrence_type", nullable = false, length = 40)
    private OccurrenceType type;

    @Column(nullable = false, length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "perceived_risk", nullable = false, length = 20)
    private PerceivedRisk perceivedRisk;

    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 10, scale = 6)
    private BigDecimal longitude;

    @Column(length = 100)
    private String neighborhood;

    @Column(length = 255)
    private String address;

    @Column(name = "image_path", length = 500)
    private String imagePath;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private OccurrenceStatus status = OccurrenceStatus.REGISTRADA;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    protected Occurrence() {
    }

    public Occurrence(
            OccurrenceCategory category,
            OccurrenceType type,
            String description,
            PerceivedRisk perceivedRisk,
            BigDecimal latitude,
            BigDecimal longitude,
            String neighborhood,
            String address,
            User user
    ) {
        this.category = Objects.requireNonNull(category);
        this.type = Objects.requireNonNull(type);
        if (!category.allows(type)) {
            throw new IllegalArgumentException("O tipo informado nao pertence a categoria selecionada");
        }
        this.description = Objects.requireNonNull(description);
        this.perceivedRisk = Objects.requireNonNull(perceivedRisk);
        this.latitude = Objects.requireNonNull(latitude);
        this.longitude = Objects.requireNonNull(longitude);
        this.neighborhood = neighborhood;
        this.address = address;
        this.user = Objects.requireNonNull(user);
    }

    @PrePersist
    private void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        status = OccurrenceStatus.REGISTRADA;
    }

    @PreUpdate
    private void preUpdate() {
        updatedAt = Instant.now();
    }

    public void changeStatus(OccurrenceStatus status) {
        this.status = Objects.requireNonNull(status);
    }

    public String replaceImage(String imagePath) {
        String previousImagePath = this.imagePath;
        this.imagePath = Objects.requireNonNull(imagePath);
        return previousImagePath;
    }

    public Long getId() {
        return id;
    }

    public OccurrenceCategory getCategory() {
        return category;
    }

    public OccurrenceType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public PerceivedRisk getPerceivedRisk() {
        return perceivedRisk;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public String getAddress() {
        return address;
    }

    public String getImagePath() {
        return imagePath;
    }

    public OccurrenceStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public User getUser() {
        return user;
    }
}
