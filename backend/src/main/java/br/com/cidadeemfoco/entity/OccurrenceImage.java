package br.com.cidadeemfoco.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table(name = "occurrence_images")
public class OccurrenceImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "occurrence_id", nullable = false)
    private Occurrence occurrence;

    @Column(name = "image_path", nullable = false, length = 500)
    private String imagePath;

    protected OccurrenceImage() {}

    public OccurrenceImage(Occurrence occurrence, String imagePath) {
        this.occurrence = Objects.requireNonNull(occurrence);
        this.imagePath = Objects.requireNonNull(imagePath);
    }

    public Long getId() { return id; }
    public Occurrence getOccurrence() { return occurrence; }
    public String getImagePath() { return imagePath; }
}
