package br.com.cidadeemfoco.repository;

import br.com.cidadeemfoco.entity.ClimateAlert;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClimateAlertRepository extends JpaRepository<ClimateAlert, Long> {
}
