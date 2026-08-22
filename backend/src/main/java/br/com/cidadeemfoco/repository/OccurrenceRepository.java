package br.com.cidadeemfoco.repository;

import br.com.cidadeemfoco.entity.Occurrence;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OccurrenceRepository extends JpaRepository<Occurrence, Long> {
}

