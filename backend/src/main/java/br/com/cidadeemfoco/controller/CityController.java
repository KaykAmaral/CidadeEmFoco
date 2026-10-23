package br.com.cidadeemfoco.controller;

import br.com.cidadeemfoco.dto.CityResponse;
import br.com.cidadeemfoco.service.CityService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cities")
public class CityController {

    private final CityService cityService;

    public CityController(CityService cityService) {
        this.cityService = cityService;
    }

    @GetMapping
    @Operation(summary = "Listar cidades disponiveis para cadastro", security = {})
    public List<CityResponse> findActive() {
        return cityService.findActive();
    }
}
