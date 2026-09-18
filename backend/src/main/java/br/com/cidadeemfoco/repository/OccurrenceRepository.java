package br.com.cidadeemfoco.repository;

import br.com.cidadeemfoco.entity.Occurrence;
import br.com.cidadeemfoco.enums.OccurrenceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface OccurrenceRepository extends JpaRepository<Occurrence, Long>, JpaSpecificationExecutor<Occurrence> {

    @Query("""
            SELECT occurrence
            FROM Occurrence occurrence
            WHERE occurrence.status <> :resolvedStatus
               OR occurrence.resolvedAt >= :resolvedSince
            ORDER BY occurrence.createdAt DESC
            """)
    List<Occurrence> findVisibleOnMap(
            @Param("resolvedStatus") OccurrenceStatus resolvedStatus,
            @Param("resolvedSince") Instant resolvedSince
    );
}
