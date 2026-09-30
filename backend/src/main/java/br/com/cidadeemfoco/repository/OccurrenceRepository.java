package br.com.cidadeemfoco.repository;

import br.com.cidadeemfoco.entity.Occurrence;
import br.com.cidadeemfoco.enums.OccurrenceStatus;
import br.com.cidadeemfoco.enums.OccurrenceType;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OccurrenceRepository extends JpaRepository<Occurrence, Long>, JpaSpecificationExecutor<Occurrence> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Occurrence o where o.id = :id")
    Optional<Occurrence> findByIdForUpdate(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select o from Occurrence o
            where o.category = br.com.cidadeemfoco.enums.OccurrenceCategory.EVENTO_NATURAL
              and o.type in :types and o.status in :statuses and o.createdAt <= :cutoff
              and not exists (select a.id from OccurrenceAutoClosure a where a.occurrence = o)
            order by o.createdAt, o.id
            """)
    List<Occurrence> findEligibleForAutoClosure(Set<OccurrenceType> types, Set<OccurrenceStatus> statuses,
                                               Instant cutoff, Pageable pageable);
}
