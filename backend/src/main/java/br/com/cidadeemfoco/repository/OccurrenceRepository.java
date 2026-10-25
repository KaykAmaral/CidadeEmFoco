package br.com.cidadeemfoco.repository;

import br.com.cidadeemfoco.entity.Occurrence;
import br.com.cidadeemfoco.enums.OccurrenceCategory;
import br.com.cidadeemfoco.enums.OccurrenceStatus;
import br.com.cidadeemfoco.enums.OccurrenceType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OccurrenceRepository extends JpaRepository<Occurrence, Long>, JpaSpecificationExecutor<Occurrence> {

    @Query("""
            SELECT occurrence
            FROM Occurrence occurrence
            WHERE occurrence.groupRoot IS NULL
              AND occurrence.city.id = :cityId
              AND (occurrence.status <> :resolvedStatus
               OR occurrence.resolvedAt >= :resolvedSince)
            ORDER BY occurrence.createdAt DESC
            """)
    List<Occurrence> findVisibleOnMap(
            @Param("cityId") Long cityId,
            @Param("resolvedStatus") OccurrenceStatus resolvedStatus,
            @Param("resolvedSince") Instant resolvedSince
    );

    List<Occurrence> findByGroupRootIsNullAndCategoryAndTypeInAndStatusInAndCreatedAtLessThanEqual(
            OccurrenceCategory category,
            Collection<OccurrenceType> types,
            Collection<OccurrenceStatus> statuses,
            Instant createdBefore
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Occurrence> findByCityIdAndGroupRootIsNullAndCategoryAndTypeAndStatusInAndCreatedAtGreaterThanEqual(
            Long cityId,
            OccurrenceCategory category,
            OccurrenceType type,
            Collection<OccurrenceStatus> statuses,
            Instant createdAfter
    );

    List<Occurrence> findByGroupRootIdAndCityIdOrderByCreatedAtAsc(Long groupRootId, Long cityId);

    Optional<Occurrence> findByIdAndCityId(Long id, Long cityId);

    Optional<Occurrence> findFirstByUserEmailIgnoreCaseOrderByCreatedAtDesc(String userEmail);

    boolean existsByGroupRootIdAndUserEmailIgnoreCase(Long groupRootId, String userEmail);
}
