package br.com.cidadeemfoco.repository;

import br.com.cidadeemfoco.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CityRepository extends JpaRepository<City, Long> {

    Optional<City> findByIbgeCodeAndActiveTrue(String ibgeCode);
}
