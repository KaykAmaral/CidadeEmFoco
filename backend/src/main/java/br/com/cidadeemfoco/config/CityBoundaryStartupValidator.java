package br.com.cidadeemfoco.config;

import br.com.cidadeemfoco.entity.City;
import br.com.cidadeemfoco.repository.CityRepository;
import br.com.cidadeemfoco.service.CityBoundaryService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(0)
@ConditionalOnProperty(
        name = "app.city-boundaries.validate-on-start",
        havingValue = "true",
        matchIfMissing = true
)
public class CityBoundaryStartupValidator implements ApplicationRunner {

    private final CityRepository cityRepository;
    private final CityBoundaryService boundaryService;

    public CityBoundaryStartupValidator(
            CityRepository cityRepository,
            CityBoundaryService boundaryService
    ) {
        this.cityRepository = cityRepository;
        this.boundaryService = boundaryService;
    }

    @Override
    public void run(ApplicationArguments args) {
        cityRepository.findAllByActiveTrueOrderByStateCodeAscNameAsc()
                .forEach(this::validateBoundary);
    }

    private void validateBoundary(City city) {
        try {
            boundaryService.getGeoJson(city);
        } catch (RuntimeException exception) {
            throw new IllegalStateException(
                    "A cidade ativa " + city.getName() + " (" + city.getIbgeCode()
                            + ") nao possui um limite GeoJSON valido",
                    exception
            );
        }
    }
}
