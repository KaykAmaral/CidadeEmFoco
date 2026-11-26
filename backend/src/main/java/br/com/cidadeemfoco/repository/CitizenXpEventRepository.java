package br.com.cidadeemfoco.repository;

import br.com.cidadeemfoco.entity.CitizenXpEvent;
import br.com.cidadeemfoco.enums.CitizenXpEventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface CitizenXpEventRepository extends JpaRepository<CitizenXpEvent, Long> {

    boolean existsByUserIdAndOccurrenceIdAndEventType(
            Long userId,
            Long occurrenceId,
            CitizenXpEventType eventType
    );

    @Query("""
            SELECT COALESCE(SUM(event.points), 0)
            FROM CitizenXpEvent event
            WHERE event.user.id = :userId
            """)
    Long totalXpByUserId(@Param("userId") Long userId);

    @Query("""
            SELECT event.user.id AS userId,
                   event.user.name AS name,
                   SUM(event.points) AS totalXp
            FROM CitizenXpEvent event
            WHERE event.user.city.id = :cityId
              AND event.createdAt >= :periodStart
              AND event.createdAt < :periodEnd
            GROUP BY event.user.id, event.user.name
            ORDER BY SUM(event.points) DESC, event.user.name ASC
            """)
    List<CitizenXpRankingProjection> rankByCityAndPeriod(
            @Param("cityId") Long cityId,
            @Param("periodStart") Instant periodStart,
            @Param("periodEnd") Instant periodEnd
    );
}
