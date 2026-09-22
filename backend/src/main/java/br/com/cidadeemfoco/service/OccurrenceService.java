package br.com.cidadeemfoco.service;

import br.com.cidadeemfoco.dto.CreateOccurrenceRequest;
import br.com.cidadeemfoco.dto.OccurrenceFilter;
import br.com.cidadeemfoco.dto.OccurrenceResponse;
import br.com.cidadeemfoco.entity.Occurrence;
import br.com.cidadeemfoco.entity.User;
import br.com.cidadeemfoco.enums.OccurrenceCategory;
import br.com.cidadeemfoco.enums.OccurrenceStatus;
import br.com.cidadeemfoco.enums.OccurrenceType;
import br.com.cidadeemfoco.enums.UserRole;
import br.com.cidadeemfoco.exception.BusinessRuleException;
import br.com.cidadeemfoco.exception.ResourceNotFoundException;
import br.com.cidadeemfoco.repository.OccurrenceRepository;
import br.com.cidadeemfoco.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class OccurrenceService {

    private static final Sort NEWEST_FIRST = Sort.by(Sort.Direction.DESC, "createdAt");
    private static final double EARTH_RADIUS_METERS = 6_371_000;
    private static final Set<OccurrenceStatus> GROUPABLE_STATUSES = Set.of(
            OccurrenceStatus.REGISTRADA,
            OccurrenceStatus.EM_ANALISE,
            OccurrenceStatus.EM_ATENDIMENTO
    );

    private final OccurrenceRepository occurrenceRepository;
    private final UserRepository userRepository;

    @Value("${app.occurrences.resolved-map-visibility:24h}")
    private Duration resolvedMapVisibility = Duration.ofHours(24);

    @Value("${app.occurrences.grouping-window-natural:2d}")
    private Duration naturalEventGroupingWindow = Duration.ofDays(2);

    @Value("${app.occurrences.grouping-window-infrastructure:30d}")
    private Duration infrastructureGroupingWindow = Duration.ofDays(30);

    @Value("${app.occurrences.grouping-radius-meters:500}")
    private double groupingRadiusMeters = 500;

    public OccurrenceService(OccurrenceRepository occurrenceRepository, UserRepository userRepository) {
        this.occurrenceRepository = occurrenceRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public OccurrenceResponse create(String userEmail, CreateOccurrenceRequest request) {
        User user = userRepository.findByEmailIgnoreCase(normalizeEmail(userEmail))
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));

        if (user.getRole() != UserRole.CITIZEN) {
            throw new BusinessRuleException("Somente cidadaos podem registrar ocorrencias");
        }
        validateCategoryAndType(request.category(), request.type());

        Occurrence occurrence = new Occurrence(
                request.category(),
                request.type(),
                request.description().trim(),
                request.perceivedRisk(),
                request.latitude(),
                request.longitude(),
                normalizeOptional(request.neighborhood()),
                normalizeOptional(request.address()),
                user
        );

        findSimilarCase(request).ifPresent(caseRoot -> {
            validateCitizenHasNotContributed(caseRoot, user);
            occurrence.joinCase(caseRoot);
            occurrenceRepository.save(caseRoot);
        });

        return OccurrenceResponse.from(occurrenceRepository.save(occurrence));
    }

    public List<OccurrenceResponse> findAll(OccurrenceFilter filter) {
        validateCategoryAndType(filter.category(), filter.type());
        validatePeriod(filter.createdFrom(), filter.createdTo());
        return occurrenceRepository.findAll(toSpecification(filter, null), NEWEST_FIRST)
                .stream()
                .map(OccurrenceResponse::from)
                .toList();
    }

    public OccurrenceResponse findById(Long id) {
        return occurrenceRepository.findById(id)
                .map(OccurrenceResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Ocorrencia nao encontrada"));
    }

    public List<OccurrenceResponse> findCaseReports(Long id) {
        Occurrence occurrence = occurrenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ocorrencia nao encontrada"));
        Occurrence caseRoot = occurrence.getCaseRoot();
        List<Occurrence> reports = new ArrayList<>();
        reports.add(caseRoot);
        reports.addAll(occurrenceRepository.findByGroupRootIdOrderByCreatedAtAsc(caseRoot.getId()));
        return reports.stream().map(OccurrenceResponse::from).toList();
    }

    public List<OccurrenceResponse> findVisibleOnMap() {
        Instant resolvedSince = Instant.now().minus(resolvedMapVisibility);
        return occurrenceRepository.findVisibleOnMap(OccurrenceStatus.RESOLVIDA, resolvedSince)
                .stream()
                .map(OccurrenceResponse::from)
                .toList();
    }

    @Transactional
    public OccurrenceResponse updateStatus(Long id, OccurrenceStatus status) {
        Occurrence occurrence = occurrenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ocorrencia nao encontrada"));
        Occurrence caseRoot = occurrence.getCaseRoot();
        caseRoot.changeStatus(status);
        return OccurrenceResponse.from(occurrenceRepository.saveAndFlush(caseRoot));
    }

    public List<OccurrenceResponse> findByUser(String userEmail) {
        OccurrenceFilter emptyFilter = new OccurrenceFilter(null, null, null, null, null, null);
        return occurrenceRepository.findAll(toSpecification(emptyFilter, normalizeEmail(userEmail)), NEWEST_FIRST)
                .stream()
                .map(OccurrenceResponse::from)
                .toList();
    }

    private Specification<Occurrence> toSpecification(OccurrenceFilter filter, String userEmail) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (userEmail == null) {
                predicates.add(criteriaBuilder.isNull(root.get("groupRoot")));
            }
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
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("createdAt"),
                        filter.createdFrom()
                ));
            }
            if (filter.createdTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("createdAt"),
                        filter.createdTo()
                ));
            }
            if (userEmail != null) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("user").get("email")),
                        userEmail
                ));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private void validateCategoryAndType(
            OccurrenceCategory category,
            OccurrenceType type
    ) {
        if (category != null && type != null && !category.allows(type)) {
            throw new BusinessRuleException("O tipo informado nao pertence a categoria selecionada");
        }
    }

    private void validatePeriod(Instant createdFrom, Instant createdTo) {
        if (createdFrom != null && createdTo != null && createdFrom.isAfter(createdTo)) {
            throw new BusinessRuleException("O inicio do periodo deve ser anterior ao fim");
        }
    }

    private Optional<Occurrence> findSimilarCase(CreateOccurrenceRequest request) {
        Instant createdAfter = Instant.now().minus(groupingWindowFor(request.category()));
        List<Occurrence> candidates = occurrenceRepository
                .findByGroupRootIsNullAndCategoryAndTypeAndStatusInAndCreatedAtGreaterThanEqual(
                        request.category(),
                        request.type(),
                        GROUPABLE_STATUSES,
                        createdAfter
                );

        if (candidates == null) {
            return Optional.empty();
        }

        return candidates.stream()
                .map(candidate -> new CaseDistance(
                        candidate,
                        distanceInMeters(
                                request.latitude().doubleValue(),
                                request.longitude().doubleValue(),
                                candidate.getLatitude().doubleValue(),
                                candidate.getLongitude().doubleValue()
                        )
                ))
                .filter(candidate -> candidate.distance() <= groupingRadiusMeters)
                .min(Comparator.comparingDouble(CaseDistance::distance))
                .map(CaseDistance::occurrence);
    }

    private Duration groupingWindowFor(OccurrenceCategory category) {
        return category == OccurrenceCategory.EVENTO_NATURAL
                ? naturalEventGroupingWindow
                : infrastructureGroupingWindow;
    }

    private void validateCitizenHasNotContributed(Occurrence caseRoot, User user) {
        boolean createdCase = caseRoot.getUser().getEmail().equalsIgnoreCase(user.getEmail());
        boolean alreadyReported = caseRoot.getId() != null
                && occurrenceRepository.existsByGroupRootIdAndUserEmailIgnoreCase(
                        caseRoot.getId(),
                        user.getEmail()
                );
        if (createdCase || alreadyReported) {
            throw new BusinessRuleException("Voce ja contribuiu para esta ocorrencia");
        }
    }

    private double distanceInMeters(
            double latitudeA,
            double longitudeA,
            double latitudeB,
            double longitudeB
    ) {
        double latitudeDistance = Math.toRadians(latitudeB - latitudeA);
        double longitudeDistance = Math.toRadians(longitudeB - longitudeA);
        double haversine = Math.sin(latitudeDistance / 2) * Math.sin(latitudeDistance / 2)
                + Math.cos(Math.toRadians(latitudeA)) * Math.cos(Math.toRadians(latitudeB))
                * Math.sin(longitudeDistance / 2) * Math.sin(longitudeDistance / 2);
        return EARTH_RADIUS_METERS * 2 * Math.atan2(Math.sqrt(haversine), Math.sqrt(1 - haversine));
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private record CaseDistance(Occurrence occurrence, double distance) {
    }
}
