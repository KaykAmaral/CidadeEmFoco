package br.com.cidadeemfoco.entity;

import br.com.cidadeemfoco.enums.AlertSeverity;
import br.com.cidadeemfoco.enums.ClimateAlertType;
import br.com.cidadeemfoco.enums.WhatsappNotificationStatus;
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

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "whatsapp_notifications")
public class WhatsappNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alert_id")
    private ClimateAlert alert;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "recipient_phone", nullable = false, length = 16)
    private String recipientPhone;

    @Column(name = "alert_title", nullable = false, length = 150)
    private String alertTitle;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "alert_type", nullable = false, length = 40)
    private ClimateAlertType alertType;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "alert_severity", nullable = false, length = 20)
    private AlertSeverity alertSeverity;

    @Column(name = "alert_description", nullable = false, length = 2000)
    private String alertDescription;

    @Column(name = "alert_start_at", nullable = false)
    private Instant alertStartAt;

    @Column(name = "alert_end_at", nullable = false)
    private Instant alertEndAt;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private WhatsappNotificationStatus status = WhatsappNotificationStatus.PENDING;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "last_error", length = 1000)
    private String lastError;

    @Column(name = "provider_message_id", length = 255)
    private String providerMessageId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "sent_at")
    private Instant sentAt;

    protected WhatsappNotification() {
    }

    public WhatsappNotification(ClimateAlert alert, User user) {
        this.alert = Objects.requireNonNull(alert);
        this.user = Objects.requireNonNull(user);
        this.recipientPhone = Objects.requireNonNull(user.getWhatsappPhone());
        this.alertTitle = alert.getTitle();
        this.alertType = alert.getType();
        this.alertSeverity = alert.getSeverity();
        this.alertDescription = alert.getDescription();
        this.alertStartAt = alert.getStartAt();
        this.alertEndAt = alert.getEndAt();
    }

    @PrePersist
    private void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    private void preUpdate() {
        updatedAt = Instant.now();
    }

    public void cancel() {
        if (status == WhatsappNotificationStatus.PENDING
                || status == WhatsappNotificationStatus.FAILED) {
            status = WhatsappNotificationStatus.CANCELLED;
        }
    }

    public void registerAttempt() {
        if (status != WhatsappNotificationStatus.PENDING
                && status != WhatsappNotificationStatus.FAILED) {
            throw new IllegalStateException("A notificacao nao esta disponivel para envio");
        }
        attemptCount++;
        lastError = null;
    }

    public void markAsSent(String providerMessageId) {
        status = WhatsappNotificationStatus.SENT;
        sentAt = Instant.now();
        lastError = null;
        this.providerMessageId = Objects.requireNonNull(providerMessageId);
    }

    public void markAsFailed(String error) {
        status = WhatsappNotificationStatus.FAILED;
        String message = Objects.requireNonNull(error);
        lastError = message.length() <= 1000 ? message : message.substring(0, 1000);
    }

    public Long getId() {
        return id;
    }

    public ClimateAlert getAlert() {
        return alert;
    }

    public User getUser() {
        return user;
    }

    public String getRecipientPhone() {
        return recipientPhone;
    }

    public String getAlertTitle() {
        return alertTitle;
    }

    public ClimateAlertType getAlertType() {
        return alertType;
    }

    public AlertSeverity getAlertSeverity() {
        return alertSeverity;
    }

    public String getAlertDescription() {
        return alertDescription;
    }

    public Instant getAlertStartAt() {
        return alertStartAt;
    }

    public Instant getAlertEndAt() {
        return alertEndAt;
    }

    public WhatsappNotificationStatus getStatus() {
        return status;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public String getLastError() {
        return lastError;
    }

    public String getProviderMessageId() {
        return providerMessageId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getSentAt() {
        return sentAt;
    }
}
