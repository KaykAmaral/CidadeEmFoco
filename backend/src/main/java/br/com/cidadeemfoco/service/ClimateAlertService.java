package br.com.cidadeemfoco.service;

import br.com.cidadeemfoco.dto.ClimateAlertResponse;
import br.com.cidadeemfoco.dto.CreateClimateAlertRequest;
import br.com.cidadeemfoco.entity.ClimateAlert;
import br.com.cidadeemfoco.exception.BusinessRuleException;
import br.com.cidadeemfoco.exception.ResourceNotFoundException;
import br.com.cidadeemfoco.repository.ClimateAlertRepository;
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

    public ClimateAlertService(ClimateAlertRepository climateAlertRepository) {
        this.climateAlertRepository = climateAlertRepository;
    }

    @Transactional
    public ClimateAlertResponse create(CreateClimateAlertRequest request) {
        if (!request.endAt().isAfter(request.startAt())) {
            throw new BusinessRuleException("O fim do alerta deve ser posterior ao inicio");
        }

        ClimateAlert alert = new ClimateAlert(
                request.title().trim(),
                request.type(),
                request.severity(),
                request.description().trim(),
                request.startAt(),
                request.endAt()
        );
        return ClimateAlertResponse.from(climateAlertRepository.save(alert));
    }

    public List<ClimateAlertResponse> findAll() {
        return climateAlertRepository.findAll(NEWEST_FIRST)
                .stream()
                .map(ClimateAlertResponse::from)
                .toList();
    }

    public ClimateAlertResponse findById(Long id) {
        return ClimateAlertResponse.from(findEntity(id));
    }

    public List<ClimateAlertResponse> findCurrentlyActive() {
        return climateAlertRepository.findCurrentlyActive(Instant.now())
                .stream()
                .map(ClimateAlertResponse::from)
                .toList();
    }

    @Transactional
    public ClimateAlertResponse activate(Long id) {
        ClimateAlert alert = findEntity(id);
        alert.activate();
        return ClimateAlertResponse.from(climateAlertRepository.saveAndFlush(alert));
    }

    @Transactional
    public ClimateAlertResponse deactivate(Long id) {
        ClimateAlert alert = findEntity(id);
        alert.deactivate();
        return ClimateAlertResponse.from(climateAlertRepository.saveAndFlush(alert));
    }

    private ClimateAlert findEntity(Long id) {
        return climateAlertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta climatico nao encontrado"));
    }
}
