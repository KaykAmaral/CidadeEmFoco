package br.com.cidadeemfoco.repository;

import br.com.cidadeemfoco.entity.ClimateAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface ClimateAlertRepository extends JpaRepository<ClimateAlert, Long> {

    @Query("""
            SELECT alert
            FROM ClimateAlert alert
            WHERE alert.active = true
              AND alert.startAt <= :now
              AND alert.endAt >= :now
            ORDER BY alert.startAt DESC
            """)
    List<ClimateAlert> findCurrentlyActive(@Param("now") Instant now);
}
