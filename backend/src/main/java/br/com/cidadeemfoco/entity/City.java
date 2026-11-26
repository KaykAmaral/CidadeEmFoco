package br.com.cidadeemfoco.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.Objects;

@Entity
@Table(
        name = "cities",
        uniqueConstraints = @UniqueConstraint(name = "uk_cities_ibge_code", columnNames = "ibge_code")
)
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "state_code", nullable = false, length = 2)
    private String stateCode;

    @Column(name = "ibge_code", nullable = false, length = 7)
    private String ibgeCode;

    @Column(nullable = false)
    private boolean active = true;

    protected City() {
    }

    public City(String name, String stateCode, String ibgeCode) {
        this.name = Objects.requireNonNull(name);
        this.stateCode = Objects.requireNonNull(stateCode);
        this.ibgeCode = Objects.requireNonNull(ibgeCode);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getStateCode() {
        return stateCode;
    }

    public String getIbgeCode() {
        return ibgeCode;
    }

    public boolean isActive() {
        return active;
    }
}
