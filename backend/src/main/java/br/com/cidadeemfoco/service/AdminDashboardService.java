package br.com.cidadeemfoco.service;

import br.com.cidadeemfoco.dto.AdminOccurrenceMetricsResponse;
import br.com.cidadeemfoco.dto.CityResponse;
import br.com.cidadeemfoco.dto.DailyOccurrenceMetricsResponse;
import br.com.cidadeemfoco.dto.NamedCountResponse;
import br.com.cidadeemfoco.dto.OccurrenceFilter;
import br.com.cidadeemfoco.entity.Occurrence;
import br.com.cidadeemfoco.entity.User;
import br.com.cidadeemfoco.enums.OccurrenceCategory;
import br.com.cidadeemfoco.enums.OccurrenceStatus;
import br.com.cidadeemfoco.enums.OccurrenceType;
import br.com.cidadeemfoco.exception.BusinessRuleException;
import br.com.cidadeemfoco.exception.ResourceNotFoundException;
import br.com.cidadeemfoco.repository.OccurrenceRepository;
import br.com.cidadeemfoco.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AdminDashboardService {

    private static final List<OccurrenceStatus> OPEN_STATUSES = List.of(
            OccurrenceStatus.REGISTRADA,
            OccurrenceStatus.EM_ANALISE,
            OccurrenceStatus.EM_ATENDIMENTO
    );

    private final OccurrenceRepository occurrenceRepository;
    private final UserRepository userRepository;
    private final ZoneId analyticsZoneId;

    public AdminDashboardService(
            OccurrenceRepository occurrenceRepository,
            UserRepository userRepository,
            @Value("${app.analytics.timezone:America/Sao_Paulo}") String analyticsTimezone
    ) {
        this.occurrenceRepository = occurrenceRepository;
        this.userRepository = userRepository;
        this.analyticsZoneId = ZoneId.of(analyticsTimezone);
    }

    public AdminOccurrenceMetricsResponse occurrenceMetrics(String adminEmail, OccurrenceFilter filter) {
        User admin = findUser(adminEmail);
        validateFilter(filter);
        List<Occurrence> cases = occurrenceRepository.findAll(toSpecification(admin.getCity().getId(), filter));

        long totalCases = cases.size();
        long totalReports = cases.stream().mapToLong(Occurrence::getStrength).sum();
        long openCases = cases.stream().filter(occurrence -> OPEN_STATUSES.contains(occurrence.getStatus())).count();
        long resolvedCases = countStatus(cases, OccurrenceStatus.RESOLVIDA);
        long notConfirmedCases = countStatus(cases, OccurrenceStatus.NAO_CONFIRMADA);
        long confirmedCases = totalCases - notConfirmedCases;
        long automaticallyResolvedCases = cases.stream().filter(Occurrence::isAutomaticallyResolved).count();

        return new AdminOccurrenceMetricsResponse(
                CityResponse.from(admin.getCity()),
                filter.createdFrom(),
                filter.createdTo(),
                totalCases,
                totalReports,
                openCases,
                resolvedCases,
                notConfirmedCases,
                automaticallyResolvedCases,
                percentage(resolvedCases, confirmedCases),
                averageResolutionTimeMinutes(cases),
                countByEnum(cases, Occurrence::getStatus, OccurrenceStatus.class),
                countByEnum(cases, Occurrence::getCategory, OccurrenceCategory.class),
                countByEnum(cases, Occurrence::getType, OccurrenceType.class),
                countByNeighborhood(cases),
                dailyEvolution(cases, filter)
        );
    }

    private Specification<Occurrence> toSpecification(Long cityId, OccurrenceFilter filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("city").get("id"), cityId));
            predicates.add(criteriaBuilder.isNull(root.get("groupRoot")));
            if (filter.category() != null) {
                predicates.add(criteriaBuilder.equal(root.get("category"), filter.category()));
            }
            if (filter.type() != null) {
                predicates.add(criteriaBuilder.equal(root.get("type"), filter.type()));
            }
            if (filter.status() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filter.status()));
            }
            if (filter.neighborhood() != null && !filter.neighborhood().isBlank()) {
                String neighborhood = "%" + filter.neighborhood().trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("neighborhood")), neighborhood));
            }
            if (filter.createdFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), filter.createdFrom()));
            }
            if (filter.createdTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), filter.createdTo()));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private long countStatus(List<Occurrence> cases, OccurrenceStatus status) {
        return cases.stream().filter(occurrence -> occurrence.getStatus() == status).count();
    }

    private BigDecimal percentage(long numerator, long denominator) {
        if (denominator == 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        return BigDecimal.valueOf(numerator)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
    }

    private Long averageResolutionTimeMinutes(List<Occurrence> cases) {
        var resolutionTimes = cases.stream()
                .filter(occurrence -> occurrence.getResolvedAt() != null && occurrence.getCreatedAt() != null)
                .mapToLong(occurrence -> Duration.between(
                        occurrence.getCreatedAt(),
                        occurrence.getResolvedAt()
                ).toMinutes())
                .filter(minutes -> minutes >= 0)
                .toArray();
        if (resolutionTimes.length == 0) {
            return null;
        }
        return Math.round(java.util.Arrays.stream(resolutionTimes).average().orElse(0));
    }

    private <E extends Enum<E>> Map<E, Long> countByEnum(
            List<Occurrence> cases,
            Function<Occurrence, E> classifier,
            Class<E> enumType
    ) {
        Map<E, Long> counts = new EnumMap<>(enumType);
        cases.forEach(occurrence -> counts.merge(classifier.apply(occurrence), 1L, Long::sum));
        return Map.copyOf(counts);
    }

    private List<NamedCountResponse> countByNeighborhood(List<Occurrence> cases) {
        return cases.stream()
                .collect(Collectors.groupingBy(
                        occurrence -> normalizeNeighborhood(occurrence.getNeighborhood()),
                        Collectors.counting()
                ))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .map(entry -> new NamedCountResponse(entry.getKey(), entry.getValue()))
                .toList();
    }

    private List<DailyOccurrenceMetricsResponse> dailyEvolution(
            List<Occurrence> cases,
            OccurrenceFilter filter
    ) {
        Map<LocalDate, DailyAccumulator> daily = new TreeMap<>();
        cases.forEach(occurrence -> {
            LocalDate createdDate = occurrence.getCreatedAt().atZone(analyticsZoneId).toLocalDate();
            daily.computeIfAbsent(createdDate, ignored -> new DailyAccumulator()).registeredCases++;

            Instant resolvedAt = occurrence.getResolvedAt();
            if (resolvedAt != null && isWithinPeriod(resolvedAt, filter)) {
                LocalDate resolvedDate = resolvedAt.atZone(analyticsZoneId).toLocalDate();
                daily.computeIfAbsent(resolvedDate, ignored -> new DailyAccumulator()).resolvedCases++;
            }
        });

        return daily.entrySet().stream()
                .map(entry -> new DailyOccurrenceMetricsResponse(
                        entry.getKey(),
                        entry.getValue().registeredCases,
                        entry.getValue().resolvedCases
                ))
                .toList();
    }

    private boolean isWithinPeriod(Instant instant, OccurrenceFilter filter) {
        return (filter.createdFrom() == null || !instant.isBefore(filter.createdFrom()))
                && (filter.createdTo() == null || !instant.isAfter(filter.createdTo()));
    }

    private String normalizeNeighborhood(String neighborhood) {
        return neighborhood == null || neighborhood.isBlank() ? "Nao informado" : neighborhood.trim();
    }

    private void validateFilter(OccurrenceFilter filter) {
        if (filter.category() != null && filter.type() != null && !filter.category().allows(filter.type())) {
            throw new BusinessRuleException("O tipo informado nao pertence a categoria selecionada");
        }
        if (filter.createdFrom() != null
                && filter.createdTo() != null
                && filter.createdFrom().isAfter(filter.createdTo())) {
            throw new BusinessRuleException("O inicio do periodo deve ser anterior ao fim");
        }
    }

    private User findUser(String email) {
        User user = userRepository.findByEmailIgnoreCase(email.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));
        if (user.getCity() == null) {
            throw new BusinessRuleException("O usuario nao possui uma cidade vinculada");
        }
        return user;
    }

    private static final class DailyAccumulator {
        private long registeredCases;
        private long resolvedCases;
    }
}
