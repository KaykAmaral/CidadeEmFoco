package br.com.cidadeemfoco.service;

import br.com.cidadeemfoco.entity.City;
import br.com.cidadeemfoco.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CityBoundaryServiceTest {

    private final CityBoundaryService service = new CityBoundaryService(
            new DefaultResourceLoader()
    );

    @Test
    void shouldAcceptCoordinatesInsidePraiaGrande() {
        assertThatNoException().isThrownBy(() -> service.validateCoordinates(
                praiaGrande(),
                new BigDecimal("-24.005000"),
                new BigDecimal("-46.402000")
        ));
    }

    @Test
    void shouldReturnPraiaGrandeGeoJson() throws Exception {
        var geoJson = service.getGeoJson(praiaGrande());
        var root = new com.fasterxml.jackson.databind.ObjectMapper().readTree(geoJson);

        assertThat(root.path("type").asText()).isEqualTo("FeatureCollection");
        assertThat(root.path("features").get(0).path("properties").path("codarea").asText())
                .isEqualTo("3541000");
    }

    @Test
    void shouldAcceptCoordinatesOnMunicipalBoundary() {
        assertThatNoException().isThrownBy(() -> service.validateCoordinates(
                praiaGrande(),
                new BigDecimal("-23.998600"),
                new BigDecimal("-46.391900")
        ));
    }

    @Test
    void shouldRejectCoordinatesOutsidePraiaGrande() {
        assertThatThrownBy(() -> service.validateCoordinates(
                praiaGrande(),
                new BigDecimal("-23.960800"),
                new BigDecimal("-46.333600")
        ))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("A localizacao da ocorrencia deve estar dentro do municipio de Praia Grande");
    }

    @Test
    void shouldRejectCityWithoutConfiguredBoundary() {
        City santos = new City("Santos", "SP", "3548500");

        assertThatThrownBy(() -> service.validateCoordinates(
                santos,
                new BigDecimal("-23.960800"),
                new BigDecimal("-46.333600")
        ))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("O limite geografico da cidade ainda nao esta configurado");
    }

    private City praiaGrande() {
        return new City("Praia Grande", "SP", "3541000");
    }
}
