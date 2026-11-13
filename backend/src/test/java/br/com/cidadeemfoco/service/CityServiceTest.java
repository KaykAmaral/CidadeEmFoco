package br.com.cidadeemfoco.service;

import br.com.cidadeemfoco.entity.City;
import br.com.cidadeemfoco.exception.ResourceNotFoundException;
import br.com.cidadeemfoco.repository.CityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CityServiceTest {

    @Mock
    private CityRepository cityRepository;

    @Mock
    private CityBoundaryService cityBoundaryService;

    private CityService cityService;

    @BeforeEach
    void setUp() {
        cityService = new CityService(cityRepository, cityBoundaryService);
    }

    @Test
    void shouldReturnBoundaryOfActiveCity() {
        City city = new City("Praia Grande", "SP", "3541000");
        String geoJson = "{\"type\":\"FeatureCollection\",\"features\":[]}";
        when(cityRepository.findByIbgeCodeAndActiveTrue("3541000")).thenReturn(Optional.of(city));
        when(cityBoundaryService.getGeoJson(city)).thenReturn(geoJson);

        var response = cityService.findBoundary("3541000");

        assertThat(response).isEqualTo(geoJson);
        verify(cityBoundaryService).getGeoJson(city);
    }

    @Test
    void shouldRejectMissingOrInactiveCity() {
        when(cityRepository.findByIbgeCodeAndActiveTrue("3548500")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cityService.findBoundary("3548500"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Cidade nao encontrada");
    }
}
