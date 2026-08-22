package br.com.cidadeemfoco.service;

import br.com.cidadeemfoco.dto.CreateOccurrenceRequest;
import br.com.cidadeemfoco.dto.OccurrenceFilter;
import br.com.cidadeemfoco.dto.OccurrenceResponse;
import br.com.cidadeemfoco.entity.Occurrence;
import br.com.cidadeemfoco.entity.User;
import br.com.cidadeemfoco.enums.OccurrenceCategory;
import br.com.cidadeemfoco.enums.OccurrenceType;
import br.com.cidadeemfoco.enums.UserRole;
import br.com.cidadeemfoco.exception.BusinessRuleException;
import br.com.cidadeemfoco.exception.ResourceNotFoundException;
import br.com.cidadeemfoco.repository.OccurrenceRepository;
import br.com.cidadeemfoco.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class OccurrenceService {

    private static final Sort NEWEST_FIRST = Sort.by(Sort.Direction.DESC, "createdAt");

    private final OccurrenceRepository occurrenceRepository;
    private final UserRepository userRepository;

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

        return OccurrenceResponse.from(occurrenceRepository.save(occurrence));
    }

    public List<OccurrenceResponse> findAll(OccurrenceFilter filter) {
        validateCategoryAndType(filter.category(), filter.type());
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

    public List<OccurrenceResponse> findByUser(String userEmail) {
        OccurrenceFilter emptyFilter = new OccurrenceFilter(null, null, null, null);
        return occurrenceRepository.findAll(toSpecification(emptyFilter, normalizeEmail(userEmail)), NEWEST_FIRST)
                .stream()
                .map(OccurrenceResponse::from)
                .toList();
    }

    private Specification<Occurrence> toSpecification(OccurrenceFilter filter, String userEmail) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
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

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
