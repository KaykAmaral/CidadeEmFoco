package br.com.cidadeemfoco.service;

import br.com.cidadeemfoco.dto.CityResponse;
import br.com.cidadeemfoco.exception.ResourceNotFoundException;
import br.com.cidadeemfoco.repository.CityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CityService {

    private final CityRepository cityRepository;
    private final CityBoundaryService cityBoundaryService;

    public CityService(CityRepository cityRepository, CityBoundaryService cityBoundaryService) {
        this.cityRepository = cityRepository;
        this.cityBoundaryService = cityBoundaryService;
    }

    public List<CityResponse> findActive() {
        return cityRepository.findAllByActiveTrueOrderByStateCodeAscNameAsc()
                .stream()
                .map(CityResponse::from)
                .toList();
    }

    public String findBoundary(String ibgeCode) {
        var city = cityRepository.findByIbgeCodeAndActiveTrue(ibgeCode)
                .orElseThrow(() -> new ResourceNotFoundException("Cidade nao encontrada"));
        return cityBoundaryService.getGeoJson(city);
    }
}
