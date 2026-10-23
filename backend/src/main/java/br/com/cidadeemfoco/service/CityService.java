package br.com.cidadeemfoco.service;

import br.com.cidadeemfoco.dto.CityResponse;
import br.com.cidadeemfoco.repository.CityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CityService {

    private final CityRepository cityRepository;

    public CityService(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    public List<CityResponse> findActive() {
        return cityRepository.findAllByActiveTrueOrderByStateCodeAscNameAsc()
                .stream()
                .map(CityResponse::from)
                .toList();
    }
}
