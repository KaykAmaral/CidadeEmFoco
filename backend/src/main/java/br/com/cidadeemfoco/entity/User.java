package br.com.cidadeemfoco.entity;

import br.com.cidadeemfoco.enums.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 254)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "whatsapp_phone", unique = true, length = 16)
    private String whatsappPhone;

    @Column(name = "whatsapp_notifications_enabled", nullable = false)
    private boolean whatsappNotificationsEnabled;

    @Column(name = "whatsapp_consent_at")
    private Instant whatsappConsentAt;

    @Column(unique = true, length = 11)
    private String cpf;

    @Column(length = 150)
    private String street;

    @Column(name = "address_number", length = 20)
    private String addressNumber;

    @Column(name = "address_complement", length = 100)
    private String addressComplement;

    @Column(length = 100)
    private String neighborhood;

    @Column(name = "postal_code", length = 8)
    private String postalCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    protected User() {
    }

    public User(String name, String email, String passwordHash, UserRole role) {
        this(name, email, passwordHash, role, null);
    }

    public User(String name, String email, String passwordHash, UserRole role, City city) {
        this.name = Objects.requireNonNull(name);
        this.email = Objects.requireNonNull(email);
        this.passwordHash = Objects.requireNonNull(passwordHash);
        this.role = Objects.requireNonNull(role);
        this.city = city;
    }

    public User(
            String name,
            String email,
            String passwordHash,
            UserRole role,
            City city,
            String cpf,
            String street,
            String addressNumber,
            String addressComplement,
            String neighborhood,
            String postalCode
    ) {
        this(name, email, passwordHash, role, city);
        this.cpf = Objects.requireNonNull(cpf);
        this.street = Objects.requireNonNull(street);
        this.addressNumber = Objects.requireNonNull(addressNumber);
        this.addressComplement = addressComplement;
        this.neighborhood = Objects.requireNonNull(neighborhood);
        this.postalCode = Objects.requireNonNull(postalCode);
    }

    @PrePersist
    private void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getWhatsappPhone() {
        return whatsappPhone;
    }

    public boolean isWhatsappNotificationsEnabled() {
        return whatsappNotificationsEnabled;
    }

    public Instant getWhatsappConsentAt() {
        return whatsappConsentAt;
    }

    public City getCity() {
        return city;
    }

    public String getCpf() {
        return cpf;
    }

    public String getStreet() {
        return street;
    }

    public String getAddressNumber() {
        return addressNumber;
    }

    public String getAddressComplement() {
        return addressComplement;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void enableWhatsappNotifications(String phone, Instant consentAt) {
        this.whatsappPhone = Objects.requireNonNull(phone);
        this.whatsappConsentAt = Objects.requireNonNull(consentAt);
        this.whatsappNotificationsEnabled = true;
    }

    public void disableWhatsappNotifications() {
        this.whatsappPhone = null;
        this.whatsappConsentAt = null;
        this.whatsappNotificationsEnabled = false;
    }

    public void updateProfile(
            String name,
            String cpf,
            String street,
            String addressNumber,
            String addressComplement,
            String neighborhood,
            String postalCode,
            City city
    ) {
        this.name = Objects.requireNonNull(name);
        this.cpf = Objects.requireNonNull(cpf);
        this.street = Objects.requireNonNull(street);
        this.addressNumber = Objects.requireNonNull(addressNumber);
        this.addressComplement = addressComplement;
        this.neighborhood = Objects.requireNonNull(neighborhood);
        this.postalCode = Objects.requireNonNull(postalCode);
        this.city = Objects.requireNonNull(city);
    }
}
