package br.com.cidadeemfoco.service;

import br.com.cidadeemfoco.entity.City;
import br.com.cidadeemfoco.enums.OccurrenceStatus;
import br.com.cidadeemfoco.enums.RankingPeriod;
import br.com.cidadeemfoco.repository.CityRankingMetricsProjection;
import br.com.cidadeemfoco.repository.CityRepository;
import br.com.cidadeemfoco.repository.OccurrenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CityRankingServiceTest {

    private static final ZoneId ZONE_ID = ZoneId.of("America/Sao_Paulo");

    @Mock
    private OccurrenceRepository occurrenceRepository;
    @Mock
    private CityRepository cityRepository;

    private CityRankingService service;
    private City praiaGrande;
    private City santos;
    private City guaruja;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-11-12T15:00:00Z"), ZONE_ID);
        service = new CityRankingService(occurrenceRepository, cityRepository, ZONE_ID, clock);
        praiaGrande = city(1L, "Praia Grande", "3541000");
        santos = city(2L, "Santos", "3548500");
        guaruja = city(3L, "Guaruja", "3518701");
    }

    @Test
    void shouldRankByResolutionRateThenResolvedVolumeAndIncludeCitiesWithoutCases() {
        when(cityRepository.findAllByActiveTrueOrderByStateCodeAscNameAsc())
                .thenReturn(List.of(guaruja, praiaGrande, santos));
        when(occurrenceRepository.summarizeCasesByCity(
                Instant.parse("2026-11-09T03:00:00Z"),
                Instant.parse("2026-11-16T03:00:00Z"),
                OccurrenceStatus.RESOLVIDA,
                OccurrenceStatus.NAO_CONFIRMADA
        )).thenReturn(List.of(
                metrics(1L, 4L, 3L, 1L),
                metrics(2L, 10L, 8L, 0L)
        ));

        var response = service.findRanking(RankingPeriod.WEEK);

        assertThat(response.periodStart()).isEqualTo("2026-11-09T03:00:00Z");
        assertThat(response.periodEnd()).isEqualTo("2026-11-16T03:00:00Z");
        assertThat(response.ranking()).extracting(item -> item.city().name())
                .containsExactly("Praia Grande", "Santos", "Guaruja");
        assertThat(response.ranking()).extracting(item -> item.resolutionRatePercent().toString())
                .containsExactly("100.00", "80.00", "0.00");
        assertThat(response.ranking()).extracting(item -> item.position())
                .containsExactly(1, 2, 3);
    }

    @Test
    void shouldUseCurrentCalendarMonthByDefaultCalculation() {
        when(cityRepository.findAllByActiveTrueOrderByStateCodeAscNameAsc()).thenReturn(List.of());
        when(occurrenceRepository.summarizeCasesByCity(
                Instant.parse("2026-11-01T03:00:00Z"),
                Instant.parse("2026-12-01T03:00:00Z"),
                OccurrenceStatus.RESOLVIDA,
                OccurrenceStatus.NAO_CONFIRMADA
        )).thenReturn(List.of());

        var response = service.findRanking(RankingPeriod.MONTH);

        assertThat(response.ranking()).isEmpty();
        assertThat(response.periodStart()).isEqualTo("2026-11-01T03:00:00Z");
        assertThat(response.periodEnd()).isEqualTo("2026-12-01T03:00:00Z");
    }

    private City city(Long id, String name, String ibgeCode) {
        City city = new City(name, "SP", ibgeCode);
        ReflectionTestUtils.setField(city, "id", id);
        return city;
    }

    private CityRankingMetricsProjection metrics(
            Long cityId,
            Long total,
            Long resolved,
            Long notConfirmed
    ) {
        return new CityRankingMetricsProjection() {
            public Long getCityId() { return cityId; }
            public Long getTotalCases() { return total; }
            public Long getResolvedCases() { return resolved; }
            public Long getNotConfirmedCases() { return notConfirmed; }
        };
    }
}
