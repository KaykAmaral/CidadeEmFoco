package br.com.cidadeemfoco.service;

import br.com.cidadeemfoco.dto.ClimateAlertResponse;
import br.com.cidadeemfoco.dto.CreateClimateAlertRequest;
import br.com.cidadeemfoco.entity.ClimateAlert;
import br.com.cidadeemfoco.entity.User;
import br.com.cidadeemfoco.event.ClimateAlertsChangedEvent;
import br.com.cidadeemfoco.event.ClimateAlertActivatedEvent;
import br.com.cidadeemfoco.event.ClimateAlertUnavailableEvent;
import br.com.cidadeemfoco.exception.BusinessRuleException;
import br.com.cidadeemfoco.exception.ResourceNotFoundException;
import br.com.cidadeemfoco.repository.ClimateAlertRepository;
import br.com.cidadeemfoco.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ClimateAlertService {

    private static final Sort NEWEST_FIRST = Sort.by(Sort.Direction.DESC, "createdAt");

    private final ClimateAlertRepository climateAlertRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final UserRepository userRepository;

    public ClimateAlertService(
            ClimateAlertRepository climateAlertRepository,
            ApplicationEventPublisher eventPublisher,
            UserRepository userRepository
    ) {
        this.climateAlertRepository = climateAlertRepository;
        this.eventPublisher = eventPublisher;
        this.userRepository = userRepository;
    }

    @Transactional
    public ClimateAlertResponse create(String adminEmail, CreateClimateAlertRequest request) {
        if (!request.endAt().isAfter(request.startAt())) {
            throw new BusinessRuleException("O fim do alerta deve ser posterior ao inicio");
        }

        ClimateAlert alert = new ClimateAlert(
                request.title().trim(),
                request.type(),
                request.severity(),
                request.description().trim(),
                request.startAt(),
                request.endAt(),
                findUser(adminEmail).getCity()
        );
        ClimateAlertResponse response = ClimateAlertResponse.from(climateAlertRepository.save(alert));
        eventPublisher.publishEvent(new ClimateAlertsChangedEvent());
        return response;
    }

    public List<ClimateAlertResponse> findAll(String adminEmail) {
        Long cityId = findUser(adminEmail).getCity().getId();
        return climateAlertRepository.findAllByCityId(cityId, NEWEST_FIRST)
                .stream()
                .map(ClimateAlertResponse::from)
                .toList();
    }

    public ClimateAlertResponse findById(String adminEmail, Long id) {
        return ClimateAlertResponse.from(findEntity(adminEmail, id));
    }

    @Transactional
    public ClimateAlertResponse update(String adminEmail, Long id, CreateClimateAlertRequest request) {
        if (!request.endAt().isAfter(request.startAt())) {
            throw new BusinessRuleException("O fim do alerta deve ser posterior ao inicio");
        }

        ClimateAlert alert = findEntity(adminEmail, id);
        alert.updateDetails(
                request.title().trim(),
                request.type(),
                request.severity(),
                request.description().trim(),
                request.startAt(),
                request.endAt()
        );
        ClimateAlertResponse response = ClimateAlertResponse.from(climateAlertRepository.saveAndFlush(alert));
        eventPublisher.publishEvent(new ClimateAlertsChangedEvent());
        return response;
    }

    public List<ClimateAlertResponse> findCurrentlyActive() {
        return climateAlertRepository.findCurrentlyActive(Instant.now())
                .stream()
                .map(ClimateAlertResponse::from)
                .toList();
    }

    public List<ClimateAlertResponse> findCurrentlyActive(String userEmail) {
        Long cityId = findUser(userEmail).getCity().getId();
        return climateAlertRepository.findCurrentlyActive(cityId, Instant.now())
                .stream()
                .map(ClimateAlertResponse::from)
                .toList();
    }

    @Transactional
    public ClimateAlertResponse activate(String adminEmail, Long id) {
        ClimateAlert alert = findEntity(adminEmail, id);
        alert.activate();
        ClimateAlertResponse response = ClimateAlertResponse.from(climateAlertRepository.saveAndFlush(alert));
        eventPublisher.publishEvent(new ClimateAlertActivatedEvent(alert.getId()));
        eventPublisher.publishEvent(new ClimateAlertsChangedEvent());
        return response;
    }

    @Transactional
    public ClimateAlertResponse deactivate(String adminEmail, Long id) {
        ClimateAlert alert = findEntity(adminEmail, id);
        alert.deactivate();
        ClimateAlertResponse response = ClimateAlertResponse.from(climateAlertRepository.saveAndFlush(alert));
        eventPublisher.publishEvent(new ClimateAlertUnavailableEvent(alert.getId()));
        eventPublisher.publishEvent(new ClimateAlertsChangedEvent());
        return response;
    }

    @Transactional
    public void delete(String adminEmail, Long id) {
        ClimateAlert alert = findEntity(adminEmail, id);
        eventPublisher.publishEvent(new ClimateAlertUnavailableEvent(id));
        climateAlertRepository.delete(alert);
        eventPublisher.publishEvent(new ClimateAlertsChangedEvent());
    }

    private ClimateAlert findEntity(String userEmail, Long id) {
        Long cityId = findUser(userEmail).getCity().getId();
        return climateAlertRepository.findByIdAndCityId(id, cityId)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta climatico nao encontrado"));
    }

    private User findUser(String email) {
        User user = userRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));
        if (user.getCity() == null) {
            throw new BusinessRuleException("O usuario nao possui uma cidade vinculada");
        }
        return user;
    }
}
