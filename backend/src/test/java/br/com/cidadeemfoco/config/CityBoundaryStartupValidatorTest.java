package br.com.cidadeemfoco.config;

import br.com.cidadeemfoco.entity.City;
import br.com.cidadeemfoco.exception.BusinessRuleException;
import br.com.cidadeemfoco.repository.CityRepository;
import br.com.cidadeemfoco.service.CityBoundaryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CityBoundaryStartupValidatorTest {

    @Mock
    private CityRepository cityRepository;
    @Mock
    private CityBoundaryService boundaryService;

    @Test
    void shouldValidateEveryActiveCityAtStartup() {
        City praiaGrande = new City("Praia Grande", "SP", "3541000");
        when(cityRepository.findAllByActiveTrueOrderByStateCodeAscNameAsc())
                .thenReturn(List.of(praiaGrande));
        CityBoundaryStartupValidator validator = new CityBoundaryStartupValidator(
                cityRepository,
                boundaryService
        );

        assertThatNoException().isThrownBy(() -> validator.run(new DefaultApplicationArguments()));

        verify(boundaryService).getGeoJson(praiaGrande);
    }

    @Test
    void shouldStopStartupWhenAnActiveCityHasNoValidBoundary() {
        City santos = new City("Santos", "SP", "3548500");
        when(cityRepository.findAllByActiveTrueOrderByStateCodeAscNameAsc())
                .thenReturn(List.of(santos));
        doThrow(new BusinessRuleException("O limite geografico da cidade ainda nao esta configurado"))
                .when(boundaryService).getGeoJson(santos);
        CityBoundaryStartupValidator validator = new CityBoundaryStartupValidator(
                cityRepository,
                boundaryService
        );

        assertThatThrownBy(() -> validator.run(new DefaultApplicationArguments()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("A cidade ativa Santos (3548500) nao possui um limite GeoJSON valido");
    }
}
