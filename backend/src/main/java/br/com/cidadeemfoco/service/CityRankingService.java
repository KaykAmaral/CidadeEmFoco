package br.com.cidadeemfoco.service;

import br.com.cidadeemfoco.dto.CityRankingEntryResponse;
import br.com.cidadeemfoco.dto.CityRankingResponse;
import br.com.cidadeemfoco.dto.CityResponse;
import br.com.cidadeemfoco.entity.City;
import br.com.cidadeemfoco.enums.OccurrenceStatus;
import br.com.cidadeemfoco.enums.RankingPeriod;
import br.com.cidadeemfoco.repository.CityRankingMetricsProjection;
import br.com.cidadeemfoco.repository.CityRepository;
import br.com.cidadeemfoco.repository.OccurrenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Transactional(readOnly = true)
public class CityRankingService {

    private final OccurrenceRepository occurrenceRepository;
    private final CityRepository cityRepository;
    private final ZoneId analyticsZoneId;
    private final Clock clock;

    @Autowired
    public CityRankingService(
            OccurrenceRepository occurrenceRepository,
            CityRepository cityRepository,
            @Value("${app.analytics.timezone:America/Sao_Paulo}") String analyticsTimezone
    ) {
        this(occurrenceRepository, cityRepository, ZoneId.of(analyticsTimezone), null);
    }

    CityRankingService(
            OccurrenceRepository occurrenceRepository,
            CityRepository cityRepository,
            ZoneId analyticsZoneId,
            Clock clock
    ) {
        this.occurrenceRepository = occurrenceRepository;
        this.cityRepository = cityRepository;
        this.analyticsZoneId = analyticsZoneId;
        this.clock = clock == null ? Clock.system(analyticsZoneId) : clock;
    }

    public CityRankingResponse findRanking(RankingPeriod period) {
        PeriodRange range = periodRange(period);
        Map<Long, CityRankingMetricsProjection> metricsByCity = occurrenceRepository
                .summarizeCasesByCity(
                        range.start(),
                        range.end(),
                        OccurrenceStatus.RESOLVIDA,
                        OccurrenceStatus.NAO_CONFIRMADA
                )
                .stream()
                .collect(Collectors.toMap(CityRankingMetricsProjection::getCityId, Function.identity()));

        List<UnrankedCity> ordered = cityRepository.findAllByActiveTrueOrderByStateCodeAscNameAsc()
                .stream()
                .map(city -> toUnranked(city, metricsByCity.get(city.getId())))
                .sorted(Comparator.comparing(UnrankedCity::resolutionRatePercent).reversed()
                        .thenComparing(Comparator.comparingLong(UnrankedCity::resolvedCases).reversed())
                        .thenComparing(Comparator.comparingLong(UnrankedCity::confirmedCases).reversed())
                        .thenComparing(item -> item.city().stateCode())
                        .thenComparing(item -> item.city().name()))
                .toList();

        List<CityRankingEntryResponse> ranking = IntStream.range(0, ordered.size())
                .mapToObj(index -> toResponse(index + 1, ordered.get(index)))
                .toList();

        return new CityRankingResponse(period, range.start(), range.end(), ranking);
    }

    private PeriodRange periodRange(RankingPeriod period) {
        LocalDate today = LocalDate.now(clock);
        LocalDate startDate = switch (period) {
            case WEEK -> today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            case MONTH -> today.withDayOfMonth(1);
            case YEAR -> today.withDayOfYear(1);
        };
        LocalDate endDate = switch (period) {
            case WEEK -> startDate.plusWeeks(1);
            case MONTH -> startDate.plusMonths(1);
            case YEAR -> startDate.plusYears(1);
        };
        return new PeriodRange(
                startDate.atStartOfDay(analyticsZoneId).toInstant(),
                endDate.atStartOfDay(analyticsZoneId).toInstant()
        );
    }

    private UnrankedCity toUnranked(City city, CityRankingMetricsProjection metrics) {
        long totalCases = value(metrics == null ? null : metrics.getTotalCases());
        long resolvedCases = value(metrics == null ? null : metrics.getResolvedCases());
        long notConfirmedCases = value(metrics == null ? null : metrics.getNotConfirmedCases());
        long confirmedCases = Math.max(0, totalCases - notConfirmedCases);
        return new UnrankedCity(
                CityResponse.from(city),
                totalCases,
                confirmedCases,
                resolvedCases,
                notConfirmedCases,
                percentage(resolvedCases, confirmedCases)
        );
    }

    private CityRankingEntryResponse toResponse(int position, UnrankedCity item) {
        return new CityRankingEntryResponse(
                position,
                item.city(),
                item.totalCases(),
                item.confirmedCases(),
                item.resolvedCases(),
                item.notConfirmedCases(),
                item.resolutionRatePercent()
        );
    }

    private long value(Long value) {
        return value == null ? 0 : value;
    }

    private BigDecimal percentage(long numerator, long denominator) {
        if (denominator == 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        return BigDecimal.valueOf(numerator)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
    }

    private record PeriodRange(Instant start, Instant end) {
    }

    private record UnrankedCity(
            CityResponse city,
            long totalCases,
            long confirmedCases,
            long resolvedCases,
            long notConfirmedCases,
            BigDecimal resolutionRatePercent
    ) {
    }
}
