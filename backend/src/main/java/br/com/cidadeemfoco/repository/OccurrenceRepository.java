package br.com.cidadeemfoco.repository;

import br.com.cidadeemfoco.entity.Occurrence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OccurrenceRepository extends JpaRepository<Occurrence, Long>, JpaSpecificationExecutor<Occurrence> {
}
