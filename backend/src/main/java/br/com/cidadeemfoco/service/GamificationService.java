package br.com.cidadeemfoco.service;

import br.com.cidadeemfoco.dto.CitizenGamificationResponse;
import br.com.cidadeemfoco.dto.CitizenRankingEntryResponse;
import br.com.cidadeemfoco.dto.CitizenRankingResponse;
import br.com.cidadeemfoco.dto.CityResponse;
import br.com.cidadeemfoco.entity.CitizenXpEvent;
import br.com.cidadeemfoco.entity.Occurrence;
import br.com.cidadeemfoco.entity.User;
import br.com.cidadeemfoco.enums.CitizenRankingPeriod;
import br.com.cidadeemfoco.enums.CitizenXpEventType;
import br.com.cidadeemfoco.enums.UserRole;
import br.com.cidadeemfoco.exception.BusinessRuleException;
import br.com.cidadeemfoco.exception.ResourceNotFoundException;
import br.com.cidadeemfoco.repository.CitizenXpEventRepository;
import br.com.cidadeemfoco.repository.OccurrenceRepository;
import br.com.cidadeemfoco.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Transactional(readOnly = true)
public class GamificationService {

    private final CitizenXpEventRepository xpEventRepository;
    private final OccurrenceRepository occurrenceRepository;
    private final UserRepository userRepository;
    private final int reportCreatedXp;
    private final int caseResolvedXp;
    private final int xpPerLevel;
    private final ZoneId analyticsZoneId;
    private final Clock clock;

    @Autowired
    public GamificationService(
            CitizenXpEventRepository xpEventRepository,
            OccurrenceRepository occurrenceRepository,
            UserRepository userRepository,
            @Value("${app.gamification.report-created-xp:10}") int reportCreatedXp,
            @Value("${app.gamification.case-resolved-xp:20}") int caseResolvedXp,
            @Value("${app.gamification.xp-per-level:100}") int xpPerLevel,
            @Value("${app.analytics.timezone:America/Sao_Paulo}") String analyticsTimezone
    ) {
        this(
                xpEventRepository,
                occurrenceRepository,
                userRepository,
                reportCreatedXp,
                caseResolvedXp,
                xpPerLevel,
                ZoneId.of(analyticsTimezone),
                null
        );
    }

    GamificationService(
            CitizenXpEventRepository xpEventRepository,
            OccurrenceRepository occurrenceRepository,
            UserRepository userRepository,
            int reportCreatedXp,
            int caseResolvedXp,
            int xpPerLevel,
            ZoneId analyticsZoneId,
            Clock clock
    ) {
        if (reportCreatedXp <= 0 || caseResolvedXp <= 0 || xpPerLevel <= 0) {
            throw new IllegalArgumentException("As configuracoes de XP devem ser positivas");
        }
        this.xpEventRepository = xpEventRepository;
        this.occurrenceRepository = occurrenceRepository;
        this.userRepository = userRepository;
        this.reportCreatedXp = reportCreatedXp;
        this.caseResolvedXp = caseResolvedXp;
        this.xpPerLevel = xpPerLevel;
        this.analyticsZoneId = analyticsZoneId;
        this.clock = clock == null ? Clock.system(analyticsZoneId) : clock;
    }

    @Transactional
    public void awardReportCreated(Occurrence occurrence) {
        award(occurrence, CitizenXpEventType.REPORT_CREATED, reportCreatedXp);
    }

    @Transactional
    public void awardCaseResolved(Occurrence caseRoot) {
        List<Occurrence> reports = new ArrayList<>();
        reports.add(caseRoot);
        reports.addAll(occurrenceRepository.findByGroupRootIdAndCityIdOrderByCreatedAtAsc(
                caseRoot.getId(),
                caseRoot.getCity().getId()
        ));
        reports.forEach(report -> award(report, CitizenXpEventType.CASE_RESOLVED, caseResolvedXp));
    }

    public CitizenGamificationResponse findMine(String userEmail) {
        User user = findUser(userEmail);
        if (user.getRole() != UserRole.CITIZEN) {
            throw new BusinessRuleException("A gamificacao individual e exclusiva para cidadaos");
        }
        return gamification(value(xpEventRepository.totalXpByUserId(user.getId())));
    }

    public CitizenRankingResponse findCitizenRanking(
            String userEmail,
            CitizenRankingPeriod period
    ) {
        User requester = findUser(userEmail);
        PeriodRange range = periodRange(period);
        var periodRanking = xpEventRepository.rankByCityAndPeriod(
                requester.getCity().getId(),
                range.queryStart(),
                range.end()
        );
        Map<Long, Long> totalXpByUser = periodRanking.stream()
                .collect(Collectors.toMap(
                        item -> item.getUserId(),
                        item -> value(xpEventRepository.totalXpByUserId(item.getUserId()))
                ));

        List<CitizenRankingEntryResponse> ranking = IntStream.range(0, periodRanking.size())
                .mapToObj(index -> {
                    var item = periodRanking.get(index);
                    long totalXp = totalXpByUser.get(item.getUserId());
                    return new CitizenRankingEntryResponse(
                            index + 1,
                            item.getUserId(),
                            item.getName(),
                            value(item.getTotalXp()),
                            totalXp,
                            level(totalXp)
                    );
                })
                .toList();

        return new CitizenRankingResponse(
                CityResponse.from(requester.getCity()),
                period,
                range.responseStart(),
                range.end(),
                ranking
        );
    }

    private void award(Occurrence occurrence, CitizenXpEventType type, int points) {
        if (!xpEventRepository.existsByUserIdAndOccurrenceIdAndEventType(
                occurrence.getUser().getId(),
                occurrence.getId(),
                type
        )) {
            xpEventRepository.save(new CitizenXpEvent(
                    occurrence.getUser(), occurrence, type, points
            ));
        }
    }

    private CitizenGamificationResponse gamification(long totalXp) {
        int level = level(totalXp);
        int xpInCurrentLevel = (int) (totalXp % xpPerLevel);
        return new CitizenGamificationResponse(
                totalXp,
                level,
                xpInCurrentLevel,
                xpPerLevel - xpInCurrentLevel
        );
    }

    private int level(long totalXp) {
        return Math.toIntExact(totalXp / xpPerLevel) + 1;
    }

    private PeriodRange periodRange(CitizenRankingPeriod period) {
        Instant now = clock.instant();
        if (period == CitizenRankingPeriod.ALL) {
            return new PeriodRange(Instant.EPOCH, null, now.plusNanos(1));
        }
        LocalDate today = LocalDate.now(clock);
        LocalDate startDate = switch (period) {
            case WEEK -> today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            case MONTH -> today.withDayOfMonth(1);
            case YEAR -> today.withDayOfYear(1);
            case ALL -> throw new IllegalStateException("Periodo ALL tratado separadamente");
        };
        LocalDate endDate = switch (period) {
            case WEEK -> startDate.plusWeeks(1);
            case MONTH -> startDate.plusMonths(1);
            case YEAR -> startDate.plusYears(1);
            case ALL -> throw new IllegalStateException("Periodo ALL tratado separadamente");
        };
        Instant start = startDate.atStartOfDay(analyticsZoneId).toInstant();
        return new PeriodRange(start, start, endDate.atStartOfDay(analyticsZoneId).toInstant());
    }

    private User findUser(String email) {
        User user = userRepository.findByEmailIgnoreCase(email.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));
        if (user.getCity() == null) {
            throw new BusinessRuleException("O usuario nao possui uma cidade vinculada");
        }
        return user;
    }

    private long value(Long value) {
        return value == null ? 0 : value;
    }

    private record PeriodRange(Instant queryStart, Instant responseStart, Instant end) {
    }
}
